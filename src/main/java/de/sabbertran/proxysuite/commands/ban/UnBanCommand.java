package de.sabbertran.proxysuite.commands.ban;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class UnBanCommand extends Command {
    private ProxySuite main;
    private UnBanCommand self;

    public UnBanCommand(ProxySuite main) {
        super("unban", null, "pardon");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "unban", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.unban")) {
                    if (args.length == 1) {
                        final String player = args[0];
                        final String sql = "SELECT " + main.getTablePrefix() + "bans.id FROM " + main.getTablePrefix() + "players, " + main
                                .getTablePrefix() + "bans WHERE " + main.getTablePrefix() + "bans.player = " + main.getTablePrefix() + "players.uuid AND LOWER(" + main.getTablePrefix() + "players.name) = " +
                                "'" + player.toLowerCase() + "' AND (" + main.getTablePrefix() + "bans.expiration IS NULL OR UNIX_TIMESTAMP(" + main.getTablePrefix() + "bans" +
                                ".expiration) > UNIX_TIMESTAMP(now())) ORDER BY " + main.getTablePrefix() + "bans.id DESC LIMIT 1";
                        main.getProxy().getScheduler().runAsync(main, new Runnable() {
                            public void run() {
                                try {
                                    ResultSet rs = main.getSQLConnection().createStatement().executeQuery(sql);
                                    if (rs.next()) {
                                        main.getBanHandler().unban(player, sender, rs.getInt(main.getTablePrefix() + "bans.id"));
                                    } else {
                                        main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                                ("command.player.notbanned").replace("%player%", player));
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else if (args.length == 2) {
                        final String player = args[0];
                        long addtime = main.getBanHandler().getAddTime(args[1]);
                        final Date expiration = new Date();
                        expiration.setTime(expiration.getTime() + addtime);
                        final String sql = "SELECT " + main.getTablePrefix() + "bans.id FROM " + main.getTablePrefix() + "players, " + main
                                .getTablePrefix() + "bans WHERE " + main.getTablePrefix() + "bans.player = " + main.getTablePrefix() + "players.uuid AND LOWER(" + main.getTablePrefix() + "players.name) = " +
                                "'" + player.toLowerCase() + "' AND (" + main.getTablePrefix() + "bans.expiration IS NULL OR UNIX_TIMESTAMP(" + main.getTablePrefix() + "bans" +
                                ".expiration) > UNIX_TIMESTAMP(now())) ORDER BY " + main.getTablePrefix() + "bans.id DESC LIMIT 1";
                        main.getProxy().getScheduler().runAsync(main, new Runnable() {
                            public void run() {
                                try {
                                    ResultSet rs = main.getSQLConnection().createStatement().executeQuery(sql);
                                    if (rs.next()) {
                                        main.getBanHandler().unban(player, expiration, sender, rs.getInt(main.getTablePrefix() + "bans.id"));
                                    } else {
                                        main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                                ("command.player.notbanned").replace("%player%", player));
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        main.getCommandHandler().sendUsage(sender, self);
                    }
                } else {
                    main.getPermissionHandler().sendMissingPermissionInfo(sender);
                }
            }
        });
    }
}
