package de.sabbertran.proxysuite.commands.portal;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.utils.Portal;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class DelPortalCommand extends Command {
    private ProxySuite main;
    private DelPortalCommand self;

    public DelPortalCommand(ProxySuite main) {
        super("delportal");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "delportal", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.delportal")) {
                    if (args.length == 1) {
                        Portal p = main.getPortalHandler().getPortal(args[0]);
                        if (p != null) {
                            main.getPortalHandler().deletePortal(p);
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("portal.deleted" +
                                    ".success").replace("%portal%", p.getName()));
                        } else {
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("portal" +
                                    ".notexists").replace("%portal%", args[0]));
                        }
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
