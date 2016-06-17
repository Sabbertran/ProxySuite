package de.sabbertran.proxysuite.commands.home;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.utils.Home;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class HomeCommand extends Command {
    private ProxySuite main;
    private HomeCommand self;

    public HomeCommand(ProxySuite main) {
        super("home");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "home", new Runnable() {
            public void run() {
                if (sender instanceof ProxiedPlayer) {
                    final ProxiedPlayer p = (ProxiedPlayer) sender;
                    if (args.length == 0) {
                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.home")) {
                            Home h = main.getHomeHandler().getHome(p.getName(), "home");
                            if (h != null) {
                                int remainingCooldown = main.getTeleportHandler().getRemainingCooldown(p);
                                boolean ignoreCooldown = main.getTeleportHandler().canIgnoreCooldown(sender);
                                if (remainingCooldown == 0 || ignoreCooldown) {
                                    main.getTeleportHandler().teleportToHome(p, h, ignoreCooldown);
                                } else {
                                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                            ("teleport.cooldown").replace("%cooldown%", "" + remainingCooldown));
                                }
                            } else {
                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                        ("home.notset.default"));
                            }
                        } else {
                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                        }
                    } else if (args.length == 1) {
                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.home")) {
                            Home h = main.getHomeHandler().getHome(p.getName(), args[0]);
                            if (h != null) {
                                int remainingCooldown = main.getTeleportHandler().getRemainingCooldown(p);
                                boolean ignoreCooldown = main.getTeleportHandler().canIgnoreCooldown(sender);
                                if (remainingCooldown == 0 || ignoreCooldown) {
                                    main.getTeleportHandler().teleportToHome(p, h, ignoreCooldown);
                                } else {
                                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                            ("teleport.cooldown").replace("%cooldown%", "" + remainingCooldown));
                                }
                            } else {
                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                        ("home.notset").replace("%home%", args[0]));
                            }
                        } else {
                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                        }
                    } else if (args.length == 2) {
                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.home.others")) {
                            final String player = args[0];
                            final String home = args[1];
                            main.getProxy().getScheduler().runAsync(main, new Runnable() {
                                public void run() {
                                    Home h = main.getHomeHandler().getHome(player, home);
                                    if (h != null) {
                                        int remainingCooldown = main.getTeleportHandler().getRemainingCooldown(p);
                                        boolean ignoreCooldown = main.getTeleportHandler().canIgnoreCooldown(sender);
                                        if (remainingCooldown == 0 || ignoreCooldown) {
                                            main.getTeleportHandler().teleportToHome(p, h, ignoreCooldown);
                                        } else {
                                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                                    ("teleport.cooldown").replace("%cooldown%", "" + remainingCooldown));
                                        }
                                    } else
                                        main.getMessageHandler().sendMessage(sender, main.getMessageHandler()
                                                .getMessage("home.notset.others").replace("%home%", home).replace
                                                        ("%player%", player));
                                }
                            });
                        } else {
                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                        }
                    } else {
                        main.getCommandHandler().sendUsage(sender, self);
                    }
                } else {
                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.noplayer"));
                }
            }
        });
    }
}
