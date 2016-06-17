package de.sabbertran.proxysuite.commands.portal;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.utils.Portal;
import de.sabbertran.proxysuite.utils.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class PortalCommand extends Command {
    private ProxySuite main;
    private PortalCommand self;

    public PortalCommand(ProxySuite main) {
        super("portal");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "portal", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.portal")) {
                    if (args.length == 1) {
                        if (sender instanceof ProxiedPlayer) {
                            ProxiedPlayer p = (ProxiedPlayer) sender;
                            Portal portal = main.getPortalHandler().getPortal(args[0]);
                            if (portal != null) {
                                Warp destination = main.getWarpHandler().getWarp(portal.getDestination(), true);
                                if (destination != null)
                                    main.getTeleportHandler().teleportToWarp(p, destination, true);
                                else
                                    main.getMessageHandler().sendMessage(p, main.getMessageHandler().getMessage("portal" +
                                            ".destination.notexists").replace("%destination%", portal.getDestination()));
                            } else
                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("portal" +
                                        ".notexists").replace("%portal%", args[0]));
                        } else {
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.noplayer"));
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
