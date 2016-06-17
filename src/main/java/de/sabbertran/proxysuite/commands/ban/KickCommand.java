package de.sabbertran.proxysuite.commands.ban;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class KickCommand extends Command {
    private ProxySuite main;
    private KickCommand self;

    public KickCommand(ProxySuite main) {
        super("kick");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "kick", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.kick")) {
                    if (args.length == 1) {
                        ProxiedPlayer p = main.getPlayerHandler().getPlayer(args[0], sender, false);
                        if (p != null) {
                            main.getBanHandler().kickPlayer(p, sender, false);
                        } else {
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                    ("command.player.notonline").replace("%player%", args[0]));
                        }
                    } else if (args.length > 0) {
                        ProxiedPlayer p = main.getPlayerHandler().getPlayer(args[0], sender, false);
                        if (p != null) {
                            String reason = "";
                            for (int i = 1; i < args.length; i++) {
                                reason += args[i] + " ";
                            }
                            if (reason.length() > 0)
                                reason = reason.substring(0, reason.length() - 1);
                            main.getBanHandler().kickPlayer(p, reason, sender, false);
                        } else {
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                    ("command.player.notonline").replace("%player%", args[0]));
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
