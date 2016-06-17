package de.sabbertran.proxysuite.commands.portal;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.utils.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SetPortalCommand extends Command {
    private ProxySuite main;
    private SetPortalCommand self;

    public SetPortalCommand(ProxySuite main) {
        super("setportal");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "setportal", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.setportal")) {
                    if (sender instanceof ProxiedPlayer) {
                        ProxiedPlayer p = (ProxiedPlayer) sender;
                        if (args.length == 3) {
                            String name = args[0];
                            String type = args[1];
                            Warp destination = main.getWarpHandler().getWarp(args[2], true);
                            if (main.getPortalHandler().getValidTypes().contains(type.toUpperCase())) {
                                if (destination != null) {
                                    main.getPortalHandler().addPortal(p, name, type, destination);
                                } else {
                                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("portal" +
                                            ".destination.notexists").replace("%destination%", args[2]));
                                }
                            } else {
                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("portal" +
                                        ".type.notsupported").replace("%type%", type));
                            }
                        } else {
                            main.getCommandHandler().sendUsage(sender, self);
                        }
                    } else {
                        main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.noplayer"));
                    }
                } else {
                    main.getPermissionHandler().sendMissingPermissionInfo(sender);
                }
            }
        });
    }
}
