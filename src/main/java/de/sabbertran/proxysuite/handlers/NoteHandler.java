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

public class NoteHandler {
    private ProxySuite main;

    public NoteHandler(ProxySuite main) {
        this.main = main;
    }

    public void addNote(String player, final String note, Location loc, CommandSender sender) {
        ProxiedPlayer p = main.getPlayerHandler().getPlayer(player, sender, false);
        String lastMessages = "";
        if (p != null && main.getMessageHandler().getLastMessages().containsKey(p)) {
            for (LoggedMessage m : main.getMessageHandler().getLastMessages().get(p)) {
                if (m != null) {
                    try {
                        String sql = "INSERT INTO " + main.getTablePrefix() + "lastMessages (player, message, date)" +
                                " VALUES ('" + p + "', '" + m.getMessage() + "', FROM_UNIXTIME(" + m.getDate()
                                .getTime() / 1000 + "))";
                        Statement stmt = main.getSQLConnection().createStatement();
                        stmt.execute(sql, Statement.RETURN_GENERATED_KEYS);
                        ResultSet rs = stmt.getGeneratedKeys();
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

        final String sql = "INSERT INTO " + main.getTablePrefix() + "notes (player, note, author, server, world, x, " +
                "y, z, pitch, yaw, lastMessages) SELECT " + main.getTablePrefix() + "players.uuid, ?, '" + (sender instanceof
                ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : "CONSOLE") + "', '" + loc.getServer()
                .getName() + "', '" + loc.getWorld() + "', '" + loc.getX() + "', '" + loc.getY() + "', '" + loc.getZ
                () + "', '" + loc.getPitch() + "', '" + loc.getYaw() + "', '" + lastMessages + "' FROM " + main
                .getTablePrefix() + "players WHERE LOWER(" + main.getTablePrefix() + "players.name) = '" + player
                .toLowerCase() + "'";
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    PreparedStatement pst = main.getSQLConnection().prepareStatement(sql);
                    pst.setString(1, note);
                    pst.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        main.getMessageHandler().sendMessageWithPermission(main.getMessageHandler().getMessage("note.created")
                        .replace("%author%", sender.getName()).replace("%player%", player).replace("%note%", note),
                "proxysuite.messages.noteinfo");
    }

    public void deleteNote(int id) {
        final String sql = "UPDATE " + main.getTablePrefix() + "notes SET deleted = '1' WHERE id = '" + id + "'";
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

    public void teleportToNote(final ProxiedPlayer p, final int id) {
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT server, world, x, " +
                            "y, z, pitch, yaw FROM " + main.getTablePrefix() + "notes WHERE id = '" + id + "'");
                    if (rs.next()) {
                        Location loc = new Location(main.getProxy().getServerInfo(rs.getString("server")), rs.getString
                                ("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("pitch"),
                                rs.getFloat("yaw"));
                        main.getTeleportHandler().teleportToLocation(p, loc, true, false);
                    } else {
                        main.getMessageHandler().sendMessage(p, main.getMessageHandler().getMessage("note.notexists")
                                .replace("%id%", "" + id));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendNoteList(final String player, final CommandSender sender) {
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT name, uuid FROM " +
                            main.getTablePrefix() + "players WHERE LOWER(name) = '" + player.toLowerCase() + "'");
                    if (rs.next()) {
                        ResultSet rs2 = main.getSQLConnection().createStatement().executeQuery("SELECT " + main
                                .getTablePrefix() + "notes.*, " + main.getTablePrefix() + "players.name AS authorName" +
                                " FROM " + main.getTablePrefix() + "notes, " + main.getTablePrefix() + "players WHERE" +
                                " " + main.getTablePrefix() + "notes.player = '" + rs.getString("uuid") + "' AND " +
                                main.getTablePrefix() + "notes.deleted = '0' AND " + main.getTablePrefix() + "players" +
                                ".uuid = '" + rs.getString("uuid") + "'");
                        if (rs2.next()) {
                            rs2.previous();
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("note" +
                                    ".list.header").replace("%player%", rs.getString("name")));
                            while (rs2.next()) {
                                String message;
                                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.notes.showadditionalinfo")) {
                                    message = main.getMessageHandler().getMessage("note.list.entry.withextrainfo");
                                } else
                                    message = main.getMessageHandler().getMessage("note.list.entry");
                                message = message.replace("%id%",
                                        "" + rs2.getInt("id")).replace("%note%", rs2.getString("note")).replace
                                        ("%dateCreated%", main.getDateFormat().format(rs2.getTimestamp("date")))
                                        .replace("%author%", rs2.getString("authorName")).replace("%server%", rs2
                                                .getString("server")).replace("%world%", rs2.getString("world"))
                                        .replace("%coordX%", "" + rs2.getInt("x")).replace("%coordY%", "" + rs2
                                                .getInt("y")).replace("%coordZ%", "" + rs2.getInt("z"));
                                main.getMessageHandler().sendMessage(sender, message);
                            }
                        } else {
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("note" +
                                    ".nofound").replace("%player%", rs.getString("name")));
                        }
                    } else {
                        main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command" +
                                ".player.noteseen").replace("%player%", player));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
