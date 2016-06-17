package de.sabbertran.proxysuite.handlers;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.utils.Location;
import de.sabbertran.proxysuite.utils.LoggedMessage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class WarningHandler {
    private ProxySuite main;

    public WarningHandler(ProxySuite main) {
        this.main = main;
    }

    public void addWarn(final String player, final String reason, Location loc, final CommandSender sender) {
        final ProxiedPlayer p = main.getPlayerHandler().getPlayer(player, sender, false);
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

        final String sql = "INSERT INTO " + main.getTablePrefix() + "warnings (player, reason, author, server, world," +
                " x, y, z, pitch, yaw, player_read, lastMessages) SELECT " + main.getTablePrefix() + "players.uuid, " +
                "?, '" + (sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : "CONSOLE") + "'," +
                " '" + (loc.getServer() != null ? loc.getServer().getName() : "") + "', '" + loc.getWorld() + "', '"
                + loc.getX() + "', '" + loc.getY() + "', '" + loc.getZ() + "', '" + loc.getPitch() + "', '" + loc
                .getYaw() + "', '" + (p != null ? 1 : 0) + "', '" + lastMessages + "' FROM " + main.getTablePrefix()
                + "players WHERE LOWER(" + main.getTablePrefix() + "players.name) = '" + player.toLowerCase() + "'";
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                boolean ban = false;
                try {
                    PreparedStatement pst = main.getSQLConnection().prepareStatement(sql);
                    pst.setString(1, reason);
                    pst.execute();

                    ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT COUNT(" + main
                            .getTablePrefix() + "warnings" + ".id) AS count FROM " + main.getTablePrefix() +
                            "warnings, " + main.getTablePrefix() + "players WHERE " + main.getTablePrefix() +
                            "warnings.player = " + main.getTablePrefix() + "players.uuid AND LOWER(" + main
                            .getTablePrefix() + "players.name) = '" + player.toLowerCase() + "' AND " + main
                            .getTablePrefix() + "warnings.deleted = '0' AND " + main.getTablePrefix() + "warnings" +
                            ".archived = '0' GROUP BY " + main.getTablePrefix() + "warnings.player");
                    ResultSet rs2 = main.getSQLConnection().createStatement().executeQuery("SELECT " + main
                            .getTablePrefix() + "warnings.reason FROM " + main.getTablePrefix() + "warnings, " + main
                            .getTablePrefix() + "players WHERE " + main.getTablePrefix() + "warnings.player = " + main
                            .getTablePrefix() + "players.uuid AND LOWER(" + main.getTablePrefix() + "players.name) = " +
                            "'" + player.toLowerCase() + "' AND " + main.getTablePrefix() + "warnings.deleted = '0' " +
                            "AND " + main.getTablePrefix() + "warnings.archived = '0'");
                    ban = rs.next() && rs.getInt("count") == main.getConfig().getInt("ProxySuite.Warnings.UntilBan");
                    if (ban) {
                        String warnReason = "";
                        while (rs2.next())
                            warnReason = warnReason + rs2.getString(main.getTablePrefix() + "warnings.reason") + "\n";
                        if (warnReason.length() > 0)
                            warnReason = warnReason.substring(0, warnReason.length() - 1);
                        main.getBanHandler().banPlayer(player, main.getMessageHandler().getMessage("ban" +
                                ".toomanywarnings.reason").replace("%warnings%", warnReason), sender);
                    } else if (p != null && main.getConfig().getBoolean("ProxySuite.Warnings.KickOnWarn")) {
                        main.getBanHandler().kickPlayer(p, reason, sender, true);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (!ban) {
                    main.getMessageHandler().sendMessageWithPermission(main.getMessageHandler().getMessage("warning" +
                            ".info").replace("%player%", (p != null ? p.getName() : player)).replace("%author%",
                            sender.getName()).replace("%reason%", reason), "proxysuite.messages.warninfo");
                }
            }
        });
    }

    public void deleteWarn(int id) {
        final String sql = "UPDATE " + main.getTablePrefix() + "warnings SET deleted = '1' WHERE id = '" + id + "'";
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    main.getSQLConnection().createStatement().execute(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void archiveWarn(int id) {
        final String sql = "UPDATE " + main.getTablePrefix() + "warnings SET archived = '1' WHERE id = '" + id + "'";
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    main.getSQLConnection().createStatement().execute(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void teleportToWarn(final ProxiedPlayer p, final int id) {
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT server, world, x, " +
                            "y, z, pitch, yaw FROM " + main.getTablePrefix() + "warnings WHERE id = '" + id + "'");
                    if (rs.next()) {
                        Location loc = new Location(main.getProxy().getServerInfo(rs.getString("server")), rs.getString
                                ("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("pitch"),
                                rs.getFloat("yaw"));
                        main.getTeleportHandler().teleportToLocation(p, loc, true, false);
                    } else {
                        main.getMessageHandler().sendMessage(p, main.getMessageHandler().getMessage
                                ("warning.notexists").replace("%id%", "" + id));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void sendWarningList(final String player, final CommandSender sender) {
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT name, uuid FROM " +
                            main.getTablePrefix() + "players WHERE LOWER(name) = '" + player.toLowerCase() + "'");
                    if (rs.next()) {
                        ResultSet rs2 = main.getSQLConnection().createStatement().executeQuery("SELECT " + main
                                .getTablePrefix() + "warnings.*, " + main.getTablePrefix() + "players.name AS " +
                                "authorName FROM " + main.getTablePrefix() + "warnings, " + main.getTablePrefix() +
                                "players WHERE " + main.getTablePrefix() + "warnings.player = '" + rs.getString
                                ("uuid") + "' AND " + main.getTablePrefix() + "warnings.deleted = '0' AND " + main
                                .getTablePrefix() + "players.uuid = " + main.getTablePrefix() + "warnings.author " +
                                "ORDER BY " + main.getTablePrefix() + "warnings.id DESC");
                        if (rs2.next()) {
                            rs2.previous();
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                    ("warning.list.header").replace("%player%", rs.getString("name")));
                            while (rs2.next()) {
                                String message;
                                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.warnings.showadditionalinfo")) {
                                    if (!rs2.getBoolean("archived"))
                                        message = main.getMessageHandler().getMessage("warning.list.entry" +
                                                ".withextrainfo");
                                    else
                                        message = main.getMessageHandler().getMessage("warning.list.entry" +
                                                ".archived.withextrainfo");
                                } else {
                                    if (!rs2.getBoolean("archived"))
                                        message = main.getMessageHandler().getMessage("warning.list.entry");
                                    else {
                                        message = main.getMessageHandler().getMessage("warning.list.entry.archived");
                                    }
                                }
                                message = message.replace("%id%",
                                        "" + rs2.getInt("id")).replace("%warning%", rs2.getString(main.getTablePrefix() + "warnings.reason"))
                                        .replace
                                                ("%dateCreated%", main.getDateFormat().format(rs2.getTimestamp(main.getTablePrefix() + "warnings" +
                                                        ".date"))).replace("%author%", rs2.getString("authorName"))
                                        .replace("%server%", rs2.getString(main.getTablePrefix() + "warnings.server")).replace("%world%", rs2
                                                .getString(main.getTablePrefix() + "warnings.world")).replace("%coordX%", "" + rs2.getInt
                                                (main.getTablePrefix() + "warnings.x")).replace("%coordY%", "" + rs2.getInt(main.getTablePrefix() + "warnings.y"))
                                        .replace("%coordZ%", "" + rs2.getInt(main.getTablePrefix() + "warnings.z"));
                                main.getMessageHandler().sendMessage(sender, message);
                            }
                        } else {
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                    ("warning.nofound").replace("%player%", rs.getString("name")));
                        }
                    } else {
                        main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                ("command.player.notseen").replace("%player%", player));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void hideWarningInfo(String player) {
        final String sql = "UPDATE " + main.getTablePrefix() + "warnings SET player_read = '1' WHERE player_read = " +
                "'0' AND player IN (SELECT uuid FROM " + main.getTablePrefix() + "players WHERE name = '" + player +
                "')";
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    main.getSQLConnection().createStatement().execute(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
