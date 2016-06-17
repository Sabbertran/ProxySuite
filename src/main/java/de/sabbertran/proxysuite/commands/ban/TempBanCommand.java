package de.sabbertran.proxysuite.commands.ban;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.Date;

public class TempBanCommand extends Command {
    private ProxySuite main;
    private TempBanCommand self;

    public TempBanCommand(ProxySuite main) {
        super("tempban", null, new String[]{"tban"});
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "tempban", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.tempban")) {
                    if (args.length == 2) {
                        String player = args[0];
                        long addtime = main.getBanHandler().getAddTime(args[1]);
                        Date expiration = new Date();
                        expiration.setTime(expiration.getTime() + addtime);
                        main.getBanHandler().tempBanPlayer(player, expiration, null, sender);
                    } else if (args.length > 2) {
                        String player = args[0];
                        String time = args[1];
                        String reason = "";
                        for (int i = 2; i < args.length; i++) {
                            reason += args[i] + " ";
                        }
                        if (reason.length() > 0)
                            reason = reason.substring(0, reason.length() - 1);
                        long addtime = main.getBanHandler().getAddTime(time);
                        Date expiration = new Date();
                        expiration.setTime(expiration.getTime() + addtime);
                        main.getBanHandler().tempBanPlayer(player, expiration, reason, sender);
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
