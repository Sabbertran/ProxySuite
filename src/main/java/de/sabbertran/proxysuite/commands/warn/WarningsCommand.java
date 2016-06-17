package de.sabbertran.proxysuite.commands.warn;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class WarningsCommand extends Command {
    private ProxySuite main;
    private WarningsCommand self;

    public WarningsCommand(ProxySuite main) {
        super("warnings", null, new String[]{"warns"});
        this.main = main;
        this.self = this;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, "warnings", new Runnable() {
            public void run() {
                if (args.length == 0) {
                    if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.warnings")) {
                        if (sender instanceof ProxiedPlayer) {
                            ProxiedPlayer p = (ProxiedPlayer) sender;
                            main.getWarningHandler().sendWarningList(p.getName(), p);
                        } else {
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.noplayer"));
                        }
                    } else {
                        main.getPermissionHandler().sendMissingPermissionInfo(sender);
                    }
                } else if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("hideinfo")) {
                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.warnings.hideinfo")) {
                            if (sender instanceof ProxiedPlayer) {
                                ProxiedPlayer p = (ProxiedPlayer) sender;
                                main.getWarningHandler().hideWarningInfo(p.getName());
                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("warning" +
                                        ".info.hid"));
                            } else {
                                main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.noplayer"));
                            }
                        } else {
                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                        }
                    } else {
                        if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.warnings.others")) {
                            String player = args[0];
                            main.getWarningHandler().sendWarningList(player, sender);
                        } else {
                            main.getPermissionHandler().sendMissingPermissionInfo(sender);
                        }
                    }
                } else {
                    main.getCommandHandler().sendUsage(sender, self);
                }
            }
        });
    }
}
