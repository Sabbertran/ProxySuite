package de.sabbertran.proxysuite.handlers;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.objects.LoggedMessage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class BanHandler {
    private ProxySuite main;

    public BanHandler(ProxySuite main) {
        this.main = main;
    }

    public void kickPlayer(ProxiedPlayer p, CommandSender sender, boolean hideMessage) {
        p.disconnect(main.getMessageHandler().translateColorCodes(main.getMessageHandler().getMessage("kick.defaultreason")));
        if (!hideMessage) {
            main.getMessageHandler().sendMessageWithPermission(main.getMessageHandler().getMessage("kick.info.default")
                    .replace("%player%", p.getName()).replace("%author%", sender.getName()), "proxysuite.messages" +
                    ".kickinfo");
        }
    }

    public void kickPlayer(ProxiedPlayer p, String reason, CommandSender sender, boolean hideMessage) {
        p.disconnect(main.getMessageHandler().translateColorCodes(reason));
        if (!hideMessage) {
            main.getMessageHandler().sendMessageWithPermission(main.getMessageHandler().getMessage("kick.info")
                    .replace("%player%", p.getName()).replace("%author%", sender.getName()).replace("%reason%",
                            reason), "proxysuite.messages.kickinfo");
        }
    }

    public void banPlayer(final String player, String reason, final CommandSender sender) {
        if (reason == null || reason.equals(""))
            reason = main.getMessageHandler().getMessage("ban.defaultreason");

        final String reason2 = reason;
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    ProxiedPlayer p = main.getPlayerHandler().getPlayer(player, sender, false);
                    String lastMessages = "";
                    if (p != null && main.getMessageHandler().getLastMessages().containsKey(p)) {
                        for (LoggedMessage m : main.getMessageHandler().getLastMessages().get(p)) {
                            if (m != null) {
                                try {
                                    String sql = "INSERT INTO " + main.getTablePrefix() + "lastMessages (player, message, date) " +
                                            "VALUES (?, ?, FROM_UNIXTIME(" + m
                                            .getDate().getTime() / 1000 + "))";
                                    PreparedStatement pst = main.getSQLConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                                    pst.setString(1, p.getUniqueId().toString());
                                    pst.setString(2, m.getMessage());
                                    pst.execute();
                                    ResultSet rs = pst.getGeneratedKeys();
                                    if (rs.next())
                                        lastMessages += rs.getInt(1) + ";";
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (lastMessages.length() > 0)
                            lastMessages = lastMessages.substring(0, lastMessages.length() - 1);
                    }

                    String sqlIns = "INSERT INTO " + main.getTablePrefix() + "bans (player, reason, author, lastMessages) SELECT "
                            + main.getTablePrefix() + "players.uuid, ?, '" + (sender instanceof ProxiedPlayer ? (
                            (ProxiedPlayer) sender).getUniqueId() : "CONSOLE") + "', '" + lastMessages + "' FROM " + main.getTablePrefix() +
                            "players WHERE LOWER(" + main.getTablePrefix() + "players.name) = '" + player.toLowerCase
                            () + "'";
                    PreparedStatement pst = main.getSQLConnection().prepareStatement(sqlIns);
                    pst.setString(1, reason2);
                    pst.execute();

                    if (p != null) {
                        p.disconnect(main.getMessageHandler().translateColorCodes(reason2));
                    }

                    main.getMessageHandler().sendMessageWithPermission(main.getMessageHandler().getMessage("ban" +
                            ".info").replace("%player%", (p != null ? p.getName() : player)).replace("%author%",
                            sender.getName()).replace("%reason%", reason2), "proxysuite.messages.baninfo");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void tempBanPlayer(final String player, final Date expiration, String reason, final CommandSender
            sender) {
        if (reason == null || reason.equals(""))
            reason = main.getMessageHandler().getMessage("ban.temp.defaultreason").replace("%expiration%", main
                    .getDateFormat().format(expiration));

        final String reason2 = reason;
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    ProxiedPlayer p = main.getPlayerHandler().getPlayer(player, sender, false);
                    String lastMessages = "";
                    if (p != null && main.getMessageHandler().getLastMessages().containsKey(p)) {
                        for (LoggedMessage m : main.getMessageHandler().getLastMessages().get(p)) {
                            if (m != null) {
                                try {
                                    String sql = "INSERT INTO " + main.getTablePrefix() + "lastMessages (player, message, date) " +
                                            "VALUES (?, ?, FROM_UNIXTIME(" + m
                                            .getDate().getTime() / 1000 + "))";
                                    PreparedStatement pst = main.getSQLConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                                    pst.setString(1, p.getUniqueId().toString());
                                    pst.setString(2, m.getMessage());
                                    pst.execute();
                                    ResultSet rs = pst.getGeneratedKeys();
                                    if (rs.next())
                                        lastMessages += rs.getInt(1) + ";";
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (lastMessages.length() > 0)
                            lastMessages = lastMessages.substring(0, lastMessages.length() - 1);
                    }

                    String sqlIns = "INSERT INTO " + main.getTablePrefix() + "bans (player, reason, expiration, " +
                            "author, lastMessages) SELECT " + main.getTablePrefix() + "players.uuid, ?, FROM_UNIXTIME(" +
                            expiration.getTime() / 1000 + ")," + " '" + (sender instanceof ProxiedPlayer ? (
                            (ProxiedPlayer) sender).getUniqueId() : "CONSOLE") + "', '" + lastMessages + "' FROM " + main.getTablePrefix() +
                            "players WHERE LOWER(" + main.getTablePrefix() + "players.name) = '" + player.toLowerCase
                            () + "'";
                    PreparedStatement pst = main.getSQLConnection().prepareStatement(sqlIns);
                    pst.setString(1, reason2);
                    pst.execute();

                    if (p != null) {
                        p.disconnect(main.getMessageHandler().translateColorCodes(reason2));
                    }
                    main.getMessageHandler().sendMessageWithPermission(main.getMessageHandler().getMessage("ban.temp" +
                            ".info").replace("%player%", (p != null ? p.getName() : player)).replace("%expiration%",
                            main.getDateFormat().format(expiration)).replace("%author%", sender.getName()).replace
                            ("%reason%", reason2), "proxysuite.messages.baninfo");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void unban(final String player, final CommandSender sender, int banId) {
        final String sql = "UPDATE " + main.getTablePrefix() + "bans SET expiration = now() WHERE id = '" + banId + "'";
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    main.getSQLConnection().createStatement().execute(sql);

                    main.getMessageHandler().sendMessageWithPermission(main.getMessageHandler().getMessage("unban" +
                            ".info").replace("%player%", player).replace("%author%", sender.getName()), "proxysuite" +
                            ".messages.unbaninfo");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void unban(final String player, final Date expiration, final CommandSender sender, int banId) {
        final String sql = "UPDATE " + main.getTablePrefix() + "bans SET expiration = FROM_UNIXTIME(" + expiration
                .getTime() / 1000 + ") WHERE " + main.getTablePrefix() + "id = '" + banId + "'";
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    main.getSQLConnection().createStatement().execute(sql);

                    main.getMessageHandler().sendMessageWithPermission(main.getMessageHandler().getMessage("unban" +
                            ".info.future").replace("%player%", player).replace("%expiration%", main.getDateFormat()
                            .format(expiration)).replace("%author%", sender.getName()), "proxysuite.messages" +
                            ".unbaninfo");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public long getAddTime(String time) {
        long addtime = 0;
        for (String s : time.split(";")) {
            if (s.toLowerCase().contains("s")) {
                s = s.toLowerCase().replace("s", "");
                addtime += Integer.parseInt(s) * 1000;
            } else if (s.toLowerCase().contains("m")) {
                s = s.toLowerCase().replace("m", "");
                addtime += Integer.parseInt(s) * 1000 * 60;
            } else if (s.toLowerCase().contains("h")) {
                s = s.toLowerCase().replace("h", "");
                addtime += Integer.parseInt(s) * 1000 * 60 * 60;
            } else if (s.toLowerCase().contains("d")) {
                s = s.toLowerCase().replace("d", "");
                addtime += Integer.parseInt(s) * 1000 * 60 * 60 * 24;
            }
        }
        return addtime;
    }
}
