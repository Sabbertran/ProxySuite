package de.sabbertran.proxysuite.commands.ban;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class BanCommand extends Command {
    private ProxySuite main;
    private BanCommand self;

    public BanCommand(ProxySuite main) {
        super("ban");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "ban", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.ban")) {
                    if (args.length == 1) {
                        String player = args[0];
                        main.getBanHandler().banPlayer(player, null, sender);
                    } else if (args.length > 1) {
                        String player = args[0];
                        String reason = "";
                        for (int i = 1; i < args.length; i++) {
                            reason += args[i] + " ";
                        }
                        if (reason.length() > 0)
                            reason = reason.substring(0, reason.length() - 1);
                        main.getBanHandler().banPlayer(player, reason, sender);
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
