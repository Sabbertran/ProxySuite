package de.sabbertran.proxysuite.commands.portal;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class PortalsCommand extends Command {
    private ProxySuite main;

    public PortalsCommand(ProxySuite main) {
        super("portals");
        this.main = main;
    }

    @Override
    public void execute(final CommandSender sender, String[] args) {
        main.getCommandHandler().executeCommand(sender, "portals", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.portals")) {
                    main.getPortalHandler().sendPortalList(sender);
                } else {
                    main.getPermissionHandler().sendMissingPermissionInfo(sender);
                }
            }
        });
    }
}
