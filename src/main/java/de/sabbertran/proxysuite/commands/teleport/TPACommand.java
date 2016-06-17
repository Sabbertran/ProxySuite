package de.sabbertran.proxysuite.commands.teleport;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.objects.PendingTeleport;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class TPACommand extends Command {
    private ProxySuite main;
    private TPACommand self;

    public TPACommand(ProxySuite main) {
        super("tpa");
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "tpa", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.tpa")) {
                    if (sender instanceof ProxiedPlayer) {
                        ProxiedPlayer p = (ProxiedPlayer) sender;
                        if (args.length == 1) {
                            ProxiedPlayer to = main.getPlayerHandler().getPlayer(args[0], sender, true);
                            if (to != null) {
                                PendingTeleport teleport = new PendingTeleport(main.getTeleportHandler(), PendingTeleport
                                        .TeleportType.TPA, p, to, main.getConfig().getInt("ProxySuite.Teleport.Timeout"));
                                main.getTeleportHandler().getPendingTeleports().add(teleport);
                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("teleport" +
                                        ".request.sent").replace("%player%", to.getName()).replace("%prefix%", main
                                        .getPlayerHandler().getPrefix(to)).replace("%suffix%", main
                                        .getPlayerHandler().getSuffix(to)));
                                main.getMessageHandler().sendMessage(to, main.getMessageHandler().getMessage("teleport" +
                                        ".tpa.request.received").replace("%player%", p.getName()).replace("%prefix%", main
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
