package de.sabbertran.proxysuite.commands.gamemode;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class GamemodeCommand extends Command {
    private ProxySuite main;
    private GamemodeCommand self;

    public GamemodeCommand(ProxySuite main) {
        super("gamemode", null, new String[]{"gm"});
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "proxysuite", new Runnable() {
            public void run() {
                if (sender instanceof ProxiedPlayer) {
                    ProxiedPlayer p = (ProxiedPlayer) sender;
                    if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.gamemode")) {
                        if (args.length == 1) {
                            if (args[0].equalsIgnoreCase("0") || args[0].equalsIgnoreCase("s") || args[0]
                                    .equalsIgnoreCase("survival")) {
                                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.gamemode" +
                                        ".survival")) {
                                    main.getPlayerHandler().setGamemode(p, "SURVIVAL");
                                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                            ("gamemode.survival"));
                                } else {
                                    main.getPermissionHandler().sendMissingPermissionInfo(sender);
                                }
                            } else if (args[0].equalsIgnoreCase("1") || args[0].equalsIgnoreCase("c") || args[0]
                                    .equalsIgnoreCase("creative")) {
                                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.gamemode" +
                                        ".creative")) {
                                    main.getPlayerHandler().setGamemode(p, "CREATIVE");
                                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                            ("gamemode.creative"));
                                } else {
                                    main.getPermissionHandler().sendMissingPermissionInfo(sender);
                                }
                            } else if (args[0].equalsIgnoreCase("2") || args[0].equalsIgnoreCase("a") || args[0]
                                    .equalsIgnoreCase("adventure")) {
                                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.gamemode" +
                                        ".adventure")) {
                                    main.getPlayerHandler().setGamemode(p, "ADVENTURE");
                                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                            ("gamemode.adventure"));
                                } else {
                                    main.getPermissionHandler().sendMissingPermissionInfo(sender);
                                }

                            } else if (args[0].equalsIgnoreCase("3") || args[0].equalsIgnoreCase("sp") || args[0]
                                    .equalsIgnoreCase("spectator")) {
                                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.gamemode" +
                                        ".spectator")) {
                                    main.getPlayerHandler().setGamemode(p, "SPECTATOR");
                                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                            ("gamemode.spectator"));
                                } else {
                                    main.getPermissionHandler().sendMissingPermissionInfo(sender);
                                }
                            } else {
                                main.getCommandHandler().sendUsage(sender, self);
                            }
                        } else if (args.length == 2) {
                            if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.gamemode" +
                                    ".others")) {
                                ProxiedPlayer pl = main.getPlayerHandler().getPlayer(args[1], sender, true);
                                if (pl != null) {
                                    if (args[0].equalsIgnoreCase("0") || args[0].equalsIgnoreCase("s") || args[0]
                                            .equalsIgnoreCase("survival")) {
                                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands" +
                                                ".gamemode" +
                                                ".survival")) {
                                            main.getPlayerHandler().setGamemode(pl, "SURVIVAL");
                                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler()
                                                    .getMessage("gamemode.survival.other").replace("%player%", pl
                                                            .getName()));
                                            main.getMessageHandler().sendMessage(pl, main.getMessageHandler()
                                                    .getMessage("gamemode.survival"));
                                        } else {
                                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                                        }
                                    } else if (args[0].equalsIgnoreCase("1") || args[0].equalsIgnoreCase("c") ||
                                            args[0].equalsIgnoreCase("creative")) {
                                        main.getPlayerHandler().setGamemode(pl, "CREATIVE");
                                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands" +
                                                ".gamemode.creative")) {
                                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler()
                                                    .getMessage("gamemode.creative.other").replace("%player%", pl
                                                            .getName()));
                                            main.getMessageHandler().sendMessage(pl, main.getMessageHandler()
                                                    .getMessage("gamemode.creative"));
                                        } else {
                                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                                        }
                                    } else if (args[0].equalsIgnoreCase("2") || args[0].equalsIgnoreCase("a") ||
                                            args[0].equalsIgnoreCase("adventure")) {
                                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands" +
                                                ".gamemode.adventure")) {
                                            main.getPlayerHandler().setGamemode(pl, "ADVENTURE");
                                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler()
                                                    .getMessage("gamemode.adventure.other").replace("%player%", pl
                                                            .getName()));
                                            main.getMessageHandler().sendMessage(pl, main.getMessageHandler()
                                                    .getMessage("gamemode.adventure"));
                                        } else {
                                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                                        }
                                    } else if (args[0].equalsIgnoreCase("3") || args[0].equalsIgnoreCase("sp") ||
                                            args[0].equalsIgnoreCase("spectator")) {
                                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands" +
                                                ".gamemode.spectator")) {
                                            main.getPlayerHandler().setGamemode(pl, "SPECTATOR");
                                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler()
                                                    .getMessage("gamemode.spectator.other").replace("%player%", pl
                                                            .getName()));
                                            main.getMessageHandler().sendMessage(pl, main.getMessageHandler()
                                                    .getMessage("gamemode.spectator"));
                                        } else {
                                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                                        }
                                    } else {
                                        main.getCommandHandler().sendUsage(sender, self);
                                    }
                                } else {
                                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                            ("command.player.notonline").replace("%player%", args[1]));
                                }
                            } else {
                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                        ("command.noplayer"));
                            }
                        } else {
                            main.getCommandHandler().sendUsage(sender, self);
                        }
                    } else {
                        main.getPermissionHandler().sendMissingPermissionInfo(sender);
                    }
                } else {
                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command" +
                            ".noplayer"));
                }
            }
        });
    }
}
