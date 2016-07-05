package de.sabbertran.proxysuite.commands.teleport;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.utils.PendingTeleport;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class TPAcceptCommand extends Command {
    private ProxySuite main;
    private TPAcceptCommand self;

    public TPAcceptCommand(ProxySuite main) {
        super("tpaccept", null, new String[]{"tpyes", "tpy"});
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "tpaccept", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.tpaccept")) {
                    if (sender instanceof ProxiedPlayer) {
                        ProxiedPlayer p = (ProxiedPlayer) sender;
                        PendingTeleport teleport = main.getTeleportHandler().getPendingTeleport(p);
                        if (teleport != null) {
                            int remainingCooldown = main.getTeleportHandler().getRemainingCooldown(teleport.getFrom());
                            boolean ignoreCooldown = main.getTeleportHandler().canIgnoreCooldown(sender);
                            if (remainingCooldown == 0 || ignoreCooldown) {
                                if (teleport.getType() == PendingTeleport.TeleportType.TPA) {
                                    main.getMessageHandler().sendMessage(teleport.getTo(), main.getMessageHandler().getMessage
                                            ("teleport.request.accepted").replace("%player%", teleport.getFrom()
                                            .getName()).replace("%prefix%", main
                                            .getPlayerHandler().getPrefix(teleport.getFrom())).replace("%suffix%", main
                                            .getPlayerHandler().getSuffix(teleport.getFrom())));
                                    main.getMessageHandler().sendMessage(teleport.getFrom(), main.getMessageHandler().getMessage
                                            ("teleport.request.accepted.other").replace("%player%", teleport.getTo()
                                            .getName()).replace("%prefix%", main
                                            .getPlayerHandler().getPrefix(teleport.getTo())).replace("%suffix%", main
                                            .getPlayerHandler().getSuffix(teleport.getTo())));
                                } else if (teleport.getType() == PendingTeleport.TeleportType.TPAHERE) {
                                    main.getMessageHandler().sendMessage(teleport.getFrom(), main.getMessageHandler().getMessage
                                            ("teleport.request.accepted").replace("%player%", teleport.getTo()
                                            .getName()).replace("%prefix%", main
                                            .getPlayerHandler().getPrefix(teleport.getTo())).replace("%suffix%", main
                                            .getPlayerHandler().getSuffix(teleport.getTo())));
                                    main.getMessageHandler().sendMessage(teleport.getTo(), main.getMessageHandler().getMessage
                                            ("teleport.request.accepted.other").replace("%player%", teleport.getFrom()
                                            .getName()).replace("%prefix%", main
                                            .getPlayerHandler().getPrefix(teleport.getFrom())).replace("%suffix%", main
                                            .getPlayerHandler().getSuffix(teleport.getFrom())));
                                }

                                main.getTeleportHandler().teleportToPlayer(teleport.getFrom(), teleport.getTo(), ignoreCooldown);
                                teleport.cancelCancel();
                                main.getTeleportHandler().getPendingTeleports().remove(teleport);
                            } else {
                                main.getMessageHandler().sendMessage(teleport.getFrom(), main.getMessageHandler()
                                        .getMessage("teleport.cooldown").replace("%cooldown%", "" + remainingCooldown));
                                main.getMessageHandler().sendMessage(teleport.getTo(), main.getMessageHandler()
                                        .getMessage("teleport.cooldown.other").replace("%player%", teleport.getFrom()
                                                .getName()).replace("%prefix%", main.getPlayerHandler().getPrefix(p))
                                        .replace("%suffix%", main.getPlayerHandler().getSuffix(p))
                                        .replace("%cooldown%", "" + remainingCooldown));
                                main.getTeleportHandler().getPendingTeleports().remove(teleport);
                            }
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
