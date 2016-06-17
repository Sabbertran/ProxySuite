package de.sabbertran.proxysuite.commands.note;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.utils.Location;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NoteCommand extends Command {
    private ProxySuite main;
    private NoteCommand self;

    public NoteCommand(ProxySuite main) {
        super("note");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "note", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.note")) {
                    if (args.length > 1) {
                        final String player = args[0];
                        main.getProxy().getScheduler().runAsync(main, new Runnable() {
                            public void run() {
                                try {
                                    final ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT name FROM " + main.getTablePrefix() + "players WHERE LOWER(name) = '" + player.toLowerCase() + "'");
                                    if (rs.next()) {
                                        final String fullName = rs.getString("name");
                                        String reason = "";
                                        for (int i = 1; i < args.length; i++) {
                                            reason += args[i] + " ";
                                        }
                                        if (reason.length() > 0)
                                            reason = reason.substring(0, reason.length() - 1);
                                        final String reason2 = reason;

                                        if (sender instanceof ProxiedPlayer) {
                                            final ProxiedPlayer p = (ProxiedPlayer) sender;
                                            main.getPositionHandler().requestPosition(p);
                                            main.getPositionHandler().addPositionRunnable(p, new Runnable() {
                                                public void run() {
                                                    main.getNoteHandler().addNote(fullName, reason2, main.getPositionHandler().getLocalPositions().remove(p.getUniqueId()), sender);
                                                }
                                            });
                                        } else {
                                            main.getNoteHandler().addNote(fullName, reason2, new Location(null, ""), sender);
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
