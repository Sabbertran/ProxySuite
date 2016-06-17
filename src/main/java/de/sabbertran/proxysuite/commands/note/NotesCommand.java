package de.sabbertran.proxysuite.commands.note;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class NotesCommand extends Command {
    private ProxySuite main;
    private NotesCommand self;

    public NotesCommand(ProxySuite main) {
        super("notes");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "notes", new Runnable() {
            public void run() {
                if (args.length == 0) {
                    if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.notes")) {
                        if (sender instanceof ProxiedPlayer) {
                            ProxiedPlayer p = (ProxiedPlayer) sender;
                            main.getNoteHandler().sendNoteList(p.getName(), p);
                        } else {
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.noplayer"));
                        }
                    } else {
                        main.getPermissionHandler().sendMissingPermissionInfo(sender);
                    }
                } else if (args.length == 1) {
                    if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.notes.others")) {
                        String player = args[0];
                        main.getNoteHandler().sendNoteList(player, sender);
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
