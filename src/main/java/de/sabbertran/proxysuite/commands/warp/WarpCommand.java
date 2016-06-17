package de.sabbertran.proxysuite.commands.warp;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.utils.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class WarpCommand extends Command {
    private ProxySuite main;
    private WarpCommand self;

    public WarpCommand(ProxySuite main) {
        super("warp");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "warp", new Runnable() {
                    public void run() {
                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.warp")) {
                            if (args.length == 1) {
                                if (sender instanceof ProxiedPlayer) {
                                    ProxiedPlayer p = (ProxiedPlayer) sender;
                                    boolean includeHidden = main.getPermissionHandler().hasPermission(sender, "proxysuite.warps.showhidden");
                                    Warp w = main.getWarpHandler().getWarp(args[0], includeHidden);
                                    if (w != null) {
                                        int remainingCooldown = main.getTeleportHandler().getRemainingCooldown(p);
                                        boolean ignoreCooldown = main.getTeleportHandler().canIgnoreCooldown(sender);
                                        if (remainingCooldown == 0 || ignoreCooldown) {
                                            main.getTeleportHandler().teleportToWarp(p, w, ignoreCooldown);
                                        } else {
                                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                                    ("teleport.cooldown").replace("%cooldown%", "" + remainingCooldown));
                                        }
                                    } else {
                                        main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                                ("warp.notexists").replace("%warp%", args[0]));
                                    }
                                } else {
                                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.noplayer"));
                                }
                            } else if (args.length == 2) {
                                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.warp.others")) {
                                    String player = args[0];
                                    ProxiedPlayer p = main.getPlayerHandler().getPlayer(player, sender, true);
                                    if (p != null) {
                                        boolean includeHidden = main.getPermissionHandler().hasPermission(sender, "proxysuite.warps.showhidden");
                                        Warp w = main.getWarpHandler().getWarp(args[1], includeHidden);
                                        if (w != null) {
                                            int remainingCooldown = main.getTeleportHandler().getRemainingCooldown(p);
                                            boolean ignoreCooldown = main.getTeleportHandler().canIgnoreCooldown(p) || main.getTeleportHandler().canIgnoreCooldown(sender);
                                            if (remainingCooldown == 0 || ignoreCooldown) {
                                                main.getTeleportHandler().teleportToWarp(p, w, ignoreCooldown);
                                            } else {
                                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                                        ("teleport.cooldown.others").replace("%player%", p.getName()).replace("%cooldown%", "" + remainingCooldown));
                                            }
                                        } else {
                                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                                    ("warp.notexists").replace("%warp%", args[0]));
                                        }
                                    } else {
                                        main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                                ("command.player.notonline").replace("%player%", args[0]));
                                    }
                                } else {
                                    main.getPermissionHandler().sendMissingPermissionInfo(sender);
                                }
                            } else {
                                main.getCommandHandler().sendUsage(sender, self);
                            }
                        } else {
                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                        }
                    }
                }

        );
    }
}
