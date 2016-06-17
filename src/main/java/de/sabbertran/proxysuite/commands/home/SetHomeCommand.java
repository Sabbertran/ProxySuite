package de.sabbertran.proxysuite.commands.home;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class SetHomeCommand extends Command {
    private ProxySuite main;
    private SetHomeCommand self;

    public SetHomeCommand(ProxySuite main) {
        super("sethome");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "sethome", new Runnable() {
            public void run() {
                if (args.length == 0) {
                    if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.sethome", false)) {
                        if (sender instanceof ProxiedPlayer) {
                            final ProxiedPlayer p = (ProxiedPlayer) sender;

                            int maximum = main.getHomeHandler().getMaximumHomes(p.getName());
                            if (main.getHomeHandler().getHome(p.getName(), "home") != null || maximum == -1 || main.getHomeHandler().getHomeAmount(p) < maximum) {
                                main.getPositionHandler().requestPosition(p);
                                main.getPositionHandler().addPositionRunnable(p, new Runnable() {
                                    public void run() {
                                        main.getHomeHandler().setHome(p.getName(), "home", main.getPositionHandler().getLocalPositions().remove(p.getUniqueId()));
                                        main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("home.set.success.default"));
                                    }
                                });
                            } else {
                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("home.set.maximum").replace("%maximum%", "" + maximum));
                            }
                        } else {
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.noplayer"));
                        }
                    } else {
                        main.getPermissionHandler().sendMissingPermissionInfo(sender);
                    }
                } else if (args.length == 1) {
                    if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.sethome", false)) {
                        if (sender instanceof ProxiedPlayer) {
                            final ProxiedPlayer p = (ProxiedPlayer) sender;
                            final String name = args[0];

                            int maximum = main.getHomeHandler().getMaximumHomes(p.getName());
                            if (main.getHomeHandler().getHome(p.getName(), name) != null || maximum == -1 || main
                                    .getHomeHandler().getHomeAmount(p) < maximum) {
                                main.getPositionHandler().requestPosition(p);
                                main.getPositionHandler().addPositionRunnable(p, new Runnable() {
                                    public void run() {
                                        main.getHomeHandler().setHome(p.getName(), name, main.getPositionHandler().getLocalPositions().remove(p.getUniqueId()));
                                        main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("home.set.success").replace("%home%", name));
                                    }
                                });
                            } else {
                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                        ("home.set.maximum").replace("%maximum%", "" + maximum));
                            }
                        } else {
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.noplayer"));
                        }
                    } else {
                        main.getPermissionHandler().sendMissingPermissionInfo(sender);
                    }
                } else if (args.length == 2) {
                    if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.sethome.others", false)) {
                        if (sender instanceof ProxiedPlayer) {
                            final ProxiedPlayer p = (ProxiedPlayer) sender;
                            final String player = args[0];
                            final String homeName = args[1];

                            main.getProxy().getScheduler().runAsync(main, new Runnable() {
                                public void run() {
                                    try {
                                        final ResultSet rs = main.getSQLConnection().createStatement().executeQuery
                                                ("SELECT name FROM " + main.getTablePrefix() + "players WHERE LOWER" +
                                                        "(name) = '" + player + "'");
                                        if (rs.next()) {
                                            final String fullName = rs.getString("name");
                                            main.getPositionHandler().requestPosition(p);
                                            main.getPositionHandler().addPositionRunnable(p, new Runnable() {
                                                public void run() {
                                                    main.getHomeHandler().setHome(player, homeName, main.getPositionHandler().getLocalPositions().remove(p.getUniqueId()));
                                                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("home.set.others.success").replace("%home%",homeName).replace("%player%", fullName));
                                                }
                                            });
                                        } else {
                                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                                    ("command.player.notseen").replace("%player%", player));
                                        }
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.noplayer"));
                        }
                    } else {
                        main.getPermissionHandler().sendMissingPermissionInfo(sender);
                    }
                } else {
                    main.getCommandHandler().sendUsage(sender, self);
                }
            }
        });
    }
}
