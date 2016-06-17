package de.sabbertran.proxysuite.commands.teleport;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class TPHereCommand extends Command {
    private ProxySuite main;
    private TPHereCommand self;

    public TPHereCommand(ProxySuite main) {
        super("tphere", null, "s");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "tphere", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.tphere")) {
                    if (args.length == 1) {
                        if (sender instanceof ProxiedPlayer) {
                            ProxiedPlayer p = (ProxiedPlayer) sender;
                            ProxiedPlayer teleport = main.getPlayerHandler().getPlayer(args[0], sender, true);
                            if (teleport != null) {
                                boolean ignoreCooldown = main.getTeleportHandler().canIgnoreCooldown(sender);
                                main.getTeleportHandler().teleportToPlayer(teleport, p, ignoreCooldown);
                            } else {
                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                        ("command.player.notonline").replace("%player%", args[0]));
                            }
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
