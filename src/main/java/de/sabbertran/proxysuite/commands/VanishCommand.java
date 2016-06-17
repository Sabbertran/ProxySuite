package de.sabbertran.proxysuite.commands;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;

public class VanishCommand extends Command {
    private ProxySuite main;

    public VanishCommand(ProxySuite main) {
        super("vanish", null, "v");
        this.main = main;
    }

    @Override
    public void execute(final CommandSender sender, String[] args) {
        main.getCommandHandler().executeCommand(sender, "vanish", new Runnable() {
            public void run() {
                if (main.getPermissionHandler().hasPermission(sender, "proxysuite.commands.vanish")) {
                    if (sender instanceof ProxiedPlayer) {
                        final ProxiedPlayer p = (ProxiedPlayer) sender;
                        if (!main.getPlayerHandler().getVanishedPlayers().contains(p)) {
                            main.getPlayerHandler().getVanishedPlayers().add(p);
                            main.getPlayerHandler().sendVanishToServer(p);
                            if (main.isBungeeTabListPlusInstalled())
                                BungeeTabListPlus.hidePlayer(p);
                            main.getProxy().getScheduler().runAsync(main, new Runnable() {
                                public void run() {
                                    String sql = "UPDATE " + main.getTablePrefix() + "players SET vanished = '1' " +
                                            "WHERE uuid = '" + p.getUniqueId() + "'";
                                    try {
                                        main.getSQLConnection().createStatement().execute(sql);
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("vanish" +
                                    ".vanished"));
                        } else {
                            main.getPlayerHandler().sendUnvanishToServer(p);
                            main.getPlayerHandler().getVanishedPlayers().remove(p);
                            if (main.isBungeeTabListPlusInstalled())
                                BungeeTabListPlus.unhidePlayer(p);
                            try {
                                String sql = "UPDATE " + main.getTablePrefix() + "players SET vanished = '0' WHERE " +
                                        "uuid = '" + p.getUniqueId() + "'";
                                main.getSQLConnection().createStatement().execute(sql);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("vanish" +
                                    ".unvanished"));
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