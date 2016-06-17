package de.sabbertran.proxysuite;

import de.sabbertran.proxysuite.utils.*;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

public class PMessageListener implements Listener {
    private ProxySuite main;

    public PMessageListener(ProxySuite main) {
        this.main = main;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent ev) {
        if (ev.getTag().equals("ProxySuite")) {
            ByteArrayInputStream stream = new ByteArrayInputStream(ev.getData());
            DataInputStream in = new DataInputStream(stream);
            try {
                String subchannel = in.readUTF();
                if (subchannel.equals("Permissions")) {
                    String player = in.readUTF();
                    String permission;
                    try {
                        while ((permission = in.readUTF()) != null) {
                            if (!main.getPermissionHandler().getPermissions().containsKey(player))
                                main.getPermissionHandler().getPermissions().put(player, new ArrayList<String>());
                            main.getPermissionHandler().getPermissions().get(player).add(permission.toLowerCase());
                        }
                    } catch (EOFException ex) {

                    }
                } else if (subchannel.equals("Position")) {
                    ProxiedPlayer p = main.getProxy().getPlayer(in.readUTF());
                    if (p != null) {
                        ServerInfo server = main.getProxy().getServerInfo(in.readUTF());
                        String world = in.readUTF();
                        double x = Double.parseDouble(in.readUTF());
                        double y = Double.parseDouble(in.readUTF());
                        double z = Double.parseDouble(in.readUTF());
                        float pitch = Float.parseFloat(in.readUTF());
                        float yaw = Float.parseFloat(in.readUTF());

                        Location loc = new Location(server, world, x, y, z, pitch, yaw);
                        main.getPositionHandler().locationReceived(p, loc);
                    }
                } else if (subchannel.equals("PortalEnter")) {
                    if (main.getConfig().getBoolean("ProxySuite.ModulesEnabled.Portal")) {
                        ProxiedPlayer p = main.getPlayerHandler().getPlayer(in.readUTF(), null, false);
                        Portal portal = main.getPortalHandler().getPortal(in.readUTF());
                        if (p != null && portal != null) {
                            Warp destination = main.getWarpHandler().getWarp(portal.getDestination(), true);
                            if (destination != null)
                                main.getTeleportHandler().teleportToWarp(p, destination, true);
                        }
                    }
                } else if (subchannel.equals("SetPortalFailed")) {
                    String name = in.readUTF();
                    String player = in.readUTF();
                    ProxiedPlayer p = main.getPlayerHandler().getPlayer(player, null, false);
                    if (p != null) {
                        main.getMessageHandler().sendMessage(p, main.getMessageHandler().getMessage("portal" +
                                ".creation.failed"));
                    }
                } else if (subchannel.equals("SetPortalSuccess")) {
                    String player = in.readUTF();
                    String name = in.readUTF();
                    String server = in.readUTF();
                    String world = in.readUTF();
                    int x1 = Integer.parseInt(in.readUTF());
                    int y1 = Integer.parseInt(in.readUTF());
                    int z1 = Integer.parseInt(in.readUTF());
                    int x2 = Integer.parseInt(in.readUTF());
                    int y2 = Integer.parseInt(in.readUTF());
                    int z2 = Integer.parseInt(in.readUTF());
                    String type = in.readUTF();
                    String destination = in.readUTF();
                    Location loc1 = new Location(main.getProxy().getServerInfo(server), world, x1, y1, z1);
                    Location loc2 = new Location(main.getProxy().getServerInfo(server), world, x2, y2, z2);
                    Portal portal = new Portal(name, type, loc1, loc2, destination);
                    main.getPortalHandler().addPortalSuccess(portal);
                    ProxiedPlayer p = main.getPlayerHandler().getPlayer(player, null, false);
                    if (p != null) {
                        main.getMessageHandler().sendMessage(p, main.getMessageHandler().getMessage("portal.creation" +
                                ".success").replace("%name%", name).replace("%destination%", destination));
                    }
                } else if (subchannel.equals("GetPortals")) {
                    ServerInfo s = main.getServerInfo(ev.getSender());
                    if (s != null)
                        main.getPortalHandler().sendPortalsToServer(s);
                } else if (subchannel.equals("Prefix")) {
                    String player = in.readUTF();
                    String prefix = in.readUTF();
                    ProxiedPlayer p = main.getPlayerHandler().getPlayer(player, null, false);
                    if (p != null)
                        main.getPlayerHandler().getPrefixes().put(p, prefix);
                } else if (subchannel.equals("Suffix")) {
                    String player = in.readUTF();
                    String suffix = in.readUTF();
                    ProxiedPlayer p = main.getPlayerHandler().getPlayer(player, null, false);
                    if (p != null)
                        main.getPlayerHandler().getSuffixes().put(p, suffix);
                } else if (subchannel.equals("DeathWithBack")) {
                    ServerInfo s = main.getServerInfo(ev.getSender());
                    String player = in.readUTF();
                    ProxiedPlayer p = main.getPlayerHandler().getPlayer(player, null, false);
                    if (p != null) {
                        String world = in.readUTF();
                        double x = Double.parseDouble(in.readUTF());
                        double y = Double.parseDouble(in.readUTF());
                        double z = Double.parseDouble(in.readUTF());
                        float pitch = Float.parseFloat(in.readUTF());
                        float yaw = Float.parseFloat(in.readUTF());
                        Location loc = new Location(s, world, x, y, z, pitch, yaw);
                        main.getTeleportHandler().savePlayerLocation(p, loc);
                    }
                } else if (subchannel.equals("CanExecuteCommand")) {
                    String player = in.readUTF();
                    String command = in.readUTF();
                    boolean canExecute = Boolean.parseBoolean(in.readUTF());
                    ProxiedPlayer p = main.getProxy().getPlayer(player);
                    if (p != null) {
                        CheckedCommand cc = new CheckedCommand(p, command, canExecute);
                        main.getCommandHandler().getCheckedCommands().add(cc);
                    }
                } else if (subchannel.equals("WorldChange")) {
                    ProxiedPlayer p = main.getProxy().getPlayer(in.readUTF());
                    if (p != null) {
                        if (main.getConfig().getBoolean("ProxySuite.ModulesEnabled.Fly")) {
                            if (!main.getPermissionHandler().hasPermission(p, "proxysuite.player.keepflyonworldchange")) {
                                if (main.getPlayerHandler().getFlying().contains(p)) {
                                    main.getPlayerHandler().sendUnflyToServer(p);
                                    main.getPlayerHandler().getFlying().remove(p);
                                    main.getPlayerHandler().writeFlyToDatabase(p, false);
                                    main.getMessageHandler().sendMessage(p, main.getMessageHandler().getMessage("fly" +
                                            ".disabled"));
                                }
                            }
                        }
                        if (main.getConfig().getBoolean("ProxySuite.ModulesEnabled.Gamemode")) {
                            if (!main.getPermissionHandler().hasPermission(p, "proxysuite.player" +
                                    ".keepgamemodeonworldchange")) {
                                main.getPlayerHandler().setGamemode(p, "SURVIVAL");
                            }
                        }
                    }
                } else if (subchannel.equals("ExecuteCommand")) {
                    String player = in.readUTF();
                    CommandSender sender = player.equalsIgnoreCase("CONSOLE") ? main.getProxy().getConsole() : main.getProxy().getPlayer(player);
                    if (sender != null) {
                        String cmd = in.readUTF();
                        if (cmd.startsWith("/"))
                            cmd = cmd.substring(1);
                        main.getProxy().getPluginManager().dispatchCommand(sender, cmd);
                    }
                } else if (subchannel.equals("PlayerWorldInfo")) {
                    ProxiedPlayer p = main.getProxy().getPlayer(in.readUTF());
                    if (p != null) {
                        WorldInfo worldInfo = new WorldInfo(in.readUTF(), Long.parseLong(in.readUTF()));
                        main.getPlayerHandler().getWorldInfos().put(p, worldInfo);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
