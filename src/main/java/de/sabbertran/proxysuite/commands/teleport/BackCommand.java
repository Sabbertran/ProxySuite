package de.sabbertran.proxysuite.commands.teleport;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BackCommand extends Command {
    private ProxySuite main;

    public BackCommand(ProxySuite main) {
        super("back");
        this.main = main;
    }

    @Override
    public void execute(final CommandSender sender, String[] args) {
        //TODO /back <name> für andere Spieler
        main.getCommandHandler().executeCommand(sender, "back", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.back")) {
                    if (sender instanceof ProxiedPlayer) {
                        ProxiedPlayer p = (ProxiedPlayer) sender;
                        if (main.getTeleportHandler().getLastPositions().containsKey(p)) {
                            int remainingCooldown = main.getTeleportHandler().getRemainingCooldown(p);
                            boolean ignoreCooldown = main.getTeleportHandler().canIgnoreCooldown(sender);
                            if (remainingCooldown == 0 || ignoreCooldown) {
                                main.getTeleportHandler().teleportToLocation(p, main.getTeleportHandler().getLastPositions().get
                                        (p), ignoreCooldown, false);
                            } else {
                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("teleport" +
                                        ".cooldown").replace("%cooldown%", "" + remainingCooldown));
                            }
                        } else {
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("teleport.back" +
                                    ".nolocation"));
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
