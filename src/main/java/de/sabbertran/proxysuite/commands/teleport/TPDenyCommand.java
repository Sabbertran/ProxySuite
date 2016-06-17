package de.sabbertran.proxysuite.commands.teleport;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.utils.PendingTeleport;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class TPDenyCommand extends Command {
    private ProxySuite main;
    private TPDenyCommand self;

    public TPDenyCommand(ProxySuite main) {
        super("tpdeny", null, new String[]{"tpno", "tpn"});
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "tpdeny", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.tpdeny")) {
                    if (sender instanceof ProxiedPlayer) {
                        ProxiedPlayer p = (ProxiedPlayer) sender;
                        PendingTeleport teleport = main.getTeleportHandler().getPendingTeleport(p);
                        if (teleport != null) {
                            if (teleport.getType() == PendingTeleport.TeleportType.TPA) {
                                main.getMessageHandler().sendMessage(teleport.getTo(), main.getMessageHandler()
                                        .getMessage("teleport.request.denied").replace("%player%", teleport.getFrom()
                                                .getName()).replace("%prefix%", main
                                                .getPlayerHandler().getPrefix(teleport.getFrom())).replace("%suffix%", main
                                                .getPlayerHandler().getSuffix(teleport.getFrom())));
                                main.getMessageHandler().sendMessage(teleport.getFrom(), main.getMessageHandler()
                                        .getMessage("teleport.request.denied.other").replace("%player%", teleport.getTo()
                                                .getName()).replace("%prefix%", main
                                                .getPlayerHandler().getPrefix(teleport.getTo())).replace("%suffix%", main
                                                .getPlayerHandler().getSuffix(teleport.getTo())));
                            } else if (teleport.getType() == PendingTeleport.TeleportType.TPAHERE) {
                                main.getMessageHandler().sendMessage(teleport.getTo(), main.getMessageHandler()
                                        .getMessage("teleport.request.denied").replace("%player%", teleport.getFrom()
                                                .getName()).replace("%prefix%", main
                                                .getPlayerHandler().getPrefix(teleport.getFrom())).replace("%suffix%", main
                                                .getPlayerHandler().getSuffix(teleport.getFrom())));
                                main.getMessageHandler().sendMessage(teleport.getTo(), main.getMessageHandler()
                                        .getMessage("teleport.request.denied.other").replace("%player%", teleport.getTo()
                                                .getName()).replace("%prefix%", main
                                                .getPlayerHandler().getPrefix(teleport.getTo())).replace("%suffix%", main
                                                .getPlayerHandler().getSuffix(teleport.getTo())));
                            }

                            teleport.cancelCancel();
                            main.getTeleportHandler().getPendingTeleports().remove(teleport);
                        } else {
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("teleport" +
                                    ".norequest"));
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
