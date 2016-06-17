package de.sabbertran.proxysuite.commands.note;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NoteInfoCommand extends Command {
    private ProxySuite main;
    private NoteInfoCommand self;

    public NoteInfoCommand(ProxySuite main) {
        super("noteinfo", null, new String[]{"notei"});
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "noteinfo", new Runnable() {
            public void run() {
                if (args.length == 1) {
                    if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.noteinfo")) {
                        try {
                            final int id = Integer.parseInt(args[0]);
                            main.getProxy().getScheduler().runAsync(main, new Runnable() {
                                public void run() {
                                    try {
                                        ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT " + main.getTablePrefix() +
                                                "notes.*, " + main.getTablePrefix() + "players.name AS authorName FROM " + main.getTablePrefix()
                                                + "notes, " + main.getTablePrefix() + "players WHERE " + main.getTablePrefix() + "notes.id = '" +
                                                id + "' AND " + main.getTablePrefix() + "notes.player = " + main.getTablePrefix() + "players.uuid");
                                        if (rs.next()) {
                                            String message = main.getMessageHandler().getMessage("note.info").replace("%id%",
                                                    "" + rs.getInt
                                                            ("id")).replace("%note%", rs.getString("note")).replace("dateCreated", main.getDateFormat()
                                                    .format(rs.getTimestamp("date"))).replace("%author%", rs.getString
                                                    ("authorName")).replace("%server%", rs.getString("server")).replace
                                                    ("%world%", rs.getString("world")).replace("%coordX%", "" + rs.getInt
                                                    ("x")).replace("%coordY%", "" + rs.getInt("y")).replace("%coordZ%", "" +
                                                    rs.getInt("z"));
                                            main.getMessageHandler().sendMessage(sender, message);
                                        } else {
                                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                                    ("note.notexists").replace("%id%", "" + id));
                                        }
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (NumberFormatException ex) {
                            main.getCommandHandler().sendUsage(sender, self);
                        }
                    } else {
                        main.getPermissionHandler().sendMissingPermissionInfo(sender);
                    }
                } else if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("teleport")) {
                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.noteinfo.teleport")) {
                            if (sender instanceof ProxiedPlayer) {
                                final ProxiedPlayer p = (ProxiedPlayer) sender;
                                try {
                                    final int id = Integer.parseInt(args[1]);
                                    main.getProxy().getScheduler().runAsync(main, new Runnable() {
                                        public void run() {
                                            try {
                                                ResultSet rs = main.getSQLConnection().createStatement().executeQuery
                                                        ("SELECT player FROM " + main.getTablePrefix() + "notes " +
                                                                "WHERE id = '" + id + "'");
                                                if (rs.next()) {
                                                    main.getNoteHandler().teleportToNote(p, id);
                                                } else {
                                                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                                            ("note.notexists").replace("%id%", "" + id));
                                                }
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                } catch (NumberFormatException ex) {
                                    main.getCommandHandler().sendUsage(sender, self);
                                }
                            } else {
                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.noplayer"));
                            }
                        } else {
                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                        }
                    } else if (args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("delete")) {
                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.noteinfo.delete")) {
                            try {
                                final int id = Integer.parseInt(args[1]);
                                main.getProxy().getScheduler().runAsync(main, new Runnable() {
                                    public void run() {
                                        try {
                                            ResultSet rs = main.getSQLConnection().createStatement().executeQuery
                                                    ("SELECT player FROM " + main.getTablePrefix() + "notes WHERE id" +
                                                            " = '" + id + "'");
                                            if (rs.next()) {
                                                main.getNoteHandler().deleteNote(id);
                                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                                        ("note.deleted.success").replace("%id%", "" + id));
                                            } else {
                                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                                        ("note.notexists").replace("%id%", "" + id));
                                            }
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (NumberFormatException ex) {
                                main.getCommandHandler().sendUsage(sender, self);
                            }
                        } else {
                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                        }
                    } else {
                        main.getCommandHandler().sendUsage(sender, self);
                    }
                } else {
                    main.getCommandHandler().sendUsage(sender, self);
                }
            }
        });
    }
}
