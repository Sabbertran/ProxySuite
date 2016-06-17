package de.sabbertran.proxysuite.commands.home;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class HomesCommand extends Command {
    private ProxySuite main;
    private HomesCommand self;

    public HomesCommand(ProxySuite main) {
        super("homes");
        this.main = main;
        this.self = self;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "homes", new Runnable() {
            public void run() {
                if (args.length == 0) {
                    if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.homes")) {
                        if (sender instanceof ProxiedPlayer) {
                            ProxiedPlayer p = (ProxiedPlayer) sender;
                            main.getHomeHandler().sendHomeList(p, p.getName());
                        } else {
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.noplayer"));
                        }
                    } else {
                        main.getPermissionHandler().sendMissingPermissionInfo(sender);
                    }
                } else if (args.length == 1) {
                    if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.homes.others")) {
                        main.getHomeHandler().sendHomeList(sender, args[0]);
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
