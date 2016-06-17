package de.sabbertran.proxysuite.commands;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class FlyCommand extends Command {
    private ProxySuite main;

    public FlyCommand(ProxySuite main) {
        super("fly");
        this.main = main;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "proxysuite", new Runnable() {
            public void run() {
                if (sender instanceof ProxiedPlayer) {
                    ProxiedPlayer p = (ProxiedPlayer) sender;
                    if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.fly")) {
                        boolean status = main.getPlayerHandler().getFlying().contains(p);

                        if (!status)
                            main.getPlayerHandler().sendFlyToServer(p);
                        else
                            main.getPlayerHandler().sendUnflyToServer(p);

                        if (!status) {
                            main.getPlayerHandler().getFlying().add(p);
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("fly" +
                                    ".enabled"));
                        } else {
                            main.getPlayerHandler().getFlying().remove(p);
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("fly" +
                                    ".disabled"));
                        }

                        main.getPlayerHandler().writeFlyToDatabase(p, !status);
                    } else {
                        main.getPermissionHandler().sendMissingPermissionInfo(sender);
                    }
                } else {
                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.noplayer"));
                }
            }
        });
    }
}
