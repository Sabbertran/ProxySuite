package de.sabbertran.proxysuite.commands;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class ProxySuiteCommand extends Command {
    private ProxySuiteCommand self;
    private ProxySuite main;

    public ProxySuiteCommand(ProxySuite main) {
        super("proxysuite", null, new String[]{"ps", "deeznutzforpresident"});
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "proxysuite", new Runnable() {
            public void run() {
                if (args.length == 1) {
                    if ((args[0].equalsIgnoreCase("reloadmessages") || args[0].equalsIgnoreCase("reloadmsg"))) {
                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.reloadmessages")) {
                            main.getMessageHandler().readMessagesFromFile();
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("messages.reload.success"));
                        } else {
                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                        }
                    } else if ((args[0].equalsIgnoreCase("reloadannouncements") || args[0].equalsIgnoreCase("reloadann"))) {
                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.reloadannouncements")) {
                            main.getAnnouncementHandler().readAnnouncementsFromFile();
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("announcements.reload.success"));
                        } else {
                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                        }
                    } else {
                        main.getCommandHandler().sendUsage(sender, self);
                    }
                } else {
                    main.getMessageHandler().sendMessage(sender, "&6Proxy&8Suite &r" + main.getDescription()
                            .getVersion() + " by &b" + main.getDescription().getAuthor());
                }
            }
        });
    }
}
