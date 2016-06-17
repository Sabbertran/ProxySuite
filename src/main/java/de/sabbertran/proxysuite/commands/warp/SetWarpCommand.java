package de.sabbertran.proxysuite.commands.warp;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SetWarpCommand extends Command {
    private ProxySuite main;
    private SetWarpCommand self;

    public SetWarpCommand(ProxySuite main) {
        super("setwarp");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "setwarp", new Runnable() {
            public void run() {
                if (sender instanceof ProxiedPlayer) {
                    if (args.length == 1) {
                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.setwarp")) {
                            final ProxiedPlayer p = (ProxiedPlayer) sender;
                            final String name = args[0];
                            main.getPositionHandler().requestPosition(p);
                            main.getPositionHandler().addPositionRunnable(p, new Runnable() {
                                public void run() {
                                    main.getWarpHandler().setWarp(name, main.getPositionHandler().getLocalPositions().remove(p.getUniqueId()), false);
                                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("warp.created.success").replace("%warp%", name));
                                }
                            });
                        } else {
                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                        }
                    } else if (args.length == 2 && args[1].equalsIgnoreCase("hidden")) {
                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.setwarp.hidden")) {
                            final ProxiedPlayer p = (ProxiedPlayer) sender;
                            final String name = args[0];
                            main.getPositionHandler().requestPosition(p);
                            main.getPositionHandler().addPositionRunnable(p, new Runnable() {
                                public void run() {
                                    main.getWarpHandler().setWarp(name, main.getPositionHandler().getLocalPositions().remove(p.getUniqueId()), true);
                                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("warp.created.hidden.success").replace("%warp%", name));
                                }
                            });
                        } else {
                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                        }
                    } else {
                        main.getCommandHandler().sendUsage(sender, self);
                    }
                } else {
                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.noplayer"));
                }
            }
        });
    }
}
