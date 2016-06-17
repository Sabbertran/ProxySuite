package de.sabbertran.proxysuite.commands.teleport;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.objects.PendingTeleport;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class TPAHereCommand extends Command {
    private ProxySuite main;
    private TPAHereCommand self;

    public TPAHereCommand(ProxySuite main) {
        super("tpahere");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "tpahere", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.tpahere")) {
                    if (sender instanceof ProxiedPlayer) {
                        ProxiedPlayer p = (ProxiedPlayer) sender;
                        if (args.length == 1) {
                            ProxiedPlayer teleporter = main.getPlayerHandler().getPlayer(args[0], sender, true);
                            if (teleporter != null) {
                                PendingTeleport teleport = new PendingTeleport(main.getTeleportHandler(), PendingTeleport
                                        .TeleportType.TPAHERE, teleporter, p, main.getConfig().getInt("ProxySuite.Teleport" +
                                        ".Timeout"));
                                main.getTeleportHandler().getPendingTeleports().add(teleport);
                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("teleport" +
                                        ".request.sent").replace("%player%", teleporter.getName()));
                                main.getMessageHandler().sendMessage(teleporter, main.getMessageHandler().getMessage
                                        ("teleport.tpahere.request.received").replace("%player%", p.getName()).replace("%prefix%", main
                                        .getPlayerHandler().getPrefix(p)).replace("%suffix%", main
                                        .getPlayerHandler().getSuffix(p)));
                            } else {
                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage
                                        ("command.player.notonline").replace("%player%", args[0]));
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
