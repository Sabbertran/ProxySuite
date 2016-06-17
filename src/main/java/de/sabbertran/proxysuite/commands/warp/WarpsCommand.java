package de.sabbertran.proxysuite.commands.warp;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class WarpsCommand extends Command {
    private ProxySuite main;

    public WarpsCommand(ProxySuite main) {
        super("warps");
        this.main = main;
    }

    @Override
    public void execute(final CommandSender sender, String[] args) {
        main.getCommandHandler().executeCommand(sender, "warps", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.warps")) {
                    boolean includeHidden = main.getPermissionHandler().hasPermission(sender, "proxysuite.warps.showhidden");
                    main.getWarpHandler().sendWarpList(sender, includeHidden);
                } else {
                    main.getPermissionHandler().sendMissingPermissionInfo(sender);
                }
            }
        });
    }
}
