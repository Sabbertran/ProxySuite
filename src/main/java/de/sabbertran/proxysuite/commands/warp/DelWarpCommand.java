package de.sabbertran.proxysuite.commands.warp;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.objects.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class DelWarpCommand extends Command {
    private ProxySuite main;
    private DelWarpCommand self;

    public DelWarpCommand(ProxySuite main) {
        super("delwarp");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "delwarp", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.delwarp")) {
                    if (args.length == 1) {
                        boolean includeHidden = main.getTeleportHandler().canIgnoreCooldown(sender);
                        Warp w = main.getWarpHandler().getWarp(args[0], includeHidden);
                        if (w != null) {
                            main.getWarpHandler().deleteWarp(w);
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("warp.deleted" +
                                    ".success").replace("%warp%", w.getName()));
                        } else {
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("warp" +
                                    ".notexists").replace("%warp%", args[0]));
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
