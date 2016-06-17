package de.sabbertran.proxysuite;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import de.sabbertran.proxysuite.utils.Location;
import de.sabbertran.proxysuite.utils.LoggedMessage;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Events implements Listener {
    private ProxySuite main;
    private ArrayList<ProxiedPlayer> justJoined;

    public Events(ProxySuite main) {
        this.main = main;
        justJoined = new ArrayList<ProxiedPlayer>();
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent ev) {
        final ProxiedPlayer p = ev.getPlayer();

        main.getProxy().getScheduler().schedule(main, new Runnable() {
            public void run() {
                main.getPermissionHandler().resetPermissions(p);
                main.getPermissionHandler().updatePermissions(p);
            }
        }, main.getConfig().getInt("ProxySuite.Server.SwitchCheckDelayMS") / 2, TimeUnit.MILLISECONDS);

        if (!justJoined.remove(p)) {
            if (main.getConfig().getBoolean("ProxySuite.ModulesEnabled.Vanish"))
                if (main.getPlayerHandler().getVanishedPlayers().contains(p))
                    main.getPlayerHandler().sendVanishToServer(p);

            main.getProxy().getScheduler().schedule(main, new Runnable() {
                public void run() {
                    if (main.getConfig().getBoolean("ProxySuite.ModulesEnabled.Fly")) {
                        if (main.getPermissionHandler().hasPermission(p, "proxysuite.player.keepflyonserverchange")) {
                            if (main.getPlayerHandler().getFlying().contains(p))
                                main.getPlayerHandler().sendFlyToServer(p);
                        } else {
                            if (main.getPlayerHandler().getFlying().contains(p)) {
                                main.getPlayerHandler().sendUnflyToServer(p);
                                main.getPlayerHandler().getFlying().remove(p);
                                main.getPlayerHandler().writeFlyToDatabase(p, false);
                                main.getMessageHandler().sendMessage(p, main.getMessageHandler().getMessage("fly.disabled"));
                            }
                        }
                    }
                    if (main.getConfig().getBoolean("ProxySuite.ModulesEnabled.Gamemode")) {
                        if (main.getPermissionHandler().hasPermission(p, "proxysuite.player.keepgamemodeonserverchange")) {
                            if (main.getPlayerHandler().getGamemode().containsKey(p))
                                main.getPlayerHandler().sendGamemodeToServer(p, main.getPlayerHandler().getGamemode().get(p));
                        } else {
                            if (!main.getPlayerHandler().getGamemode().get(p).equals("SURVIVAL"))
                                main.getPlayerHandler().setGamemode(p, "SURVIVAL");
                        }
                    }
                }
            }, main.getConfig().getInt("ProxySuite.Server.SwitchCheckDelayMS"), TimeUnit.MILLISECONDS);
        } else {
            Location firstSpawn = main.getSpawnHandler().getFirstSpawn();
            if (firstSpawn != null)
                if (main.getConfig().getBoolean("ProxySuite.ModulesEnabled.Teleport"))
                    if (main.getPlayerHandler().getPendingFirstSpawnTeleports().remove(p.getUniqueId())) {
                        main.getTeleportHandler().teleportToLocation(p, firstSpawn, true, true);
                    }
        }
    }

    @EventHandler
    public void onLogin(LoginEvent ev) {
        ev.registerIntent(main);
        try {
            ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT * FROM " + main.getTablePrefix() + "bans " +
                    "WHERE player = '" + ev
                    .getConnection().getUniqueId() + "' ORDER BY -(expiration IS NULL), expiration DESC LIMIT 1");
            if (rs.next()) {
                Date expiration = rs.getTimestamp("expiration");
                if (rs.wasNull() || expiration.after(new Date())) {
                    ev.setCancelled(true);
                    String reason;
                    if (rs.wasNull())
                        reason = main.getMessageHandler().getMessage("join.banned").replace("%reason%", rs.getString
                                ("reason"));
                    else
                        reason = main.getMessageHandler().getMessage("join.banneduntil").replace("%reason%", rs
                                .getString("reason")).replace("%expiration%", main.getDateFormat().format(expiration));
                    ev.setCancelReason(reason);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String ip = ev.getConnection().getAddress().getAddress().toString();
        if (!ev.isCancelled() && main.getConfig().getInt("ProxySuite.Security.MaxPlayersWithSameIP") > 0) {
            if (main.getPlayerHandler().getIps().containsKey(ip) && main.getPlayerHandler().getIps().get(ip) >= main
                    .getConfig().getInt("ProxySuite.Security.MaxPlayersWithSameIP")) {
                ev.setCancelled(true);
                ev.setCancelReason(main.getMessageHandler().getMessage("security.join.denied"));
            }
        }

        if (!ev.isCancelled()) {
            main.getPlayerHandler().registerLogin(ev.getConnection());

            if (main.getPlayerHandler().getIps().containsKey(ip))
                main.getPlayerHandler().getIps().put(ip, main.getPlayerHandler().getIps().get(ip) + 1);
            else
                main.getPlayerHandler().getIps().put(ip, 1);
        }


        ev.completeIntent(main);
    }

    @EventHandler
    public void onPostLogin(final PostLoginEvent ev) {
        final ProxiedPlayer p = ev.getPlayer();
        justJoined.add(p);

        if (main.getConfig().getBoolean("ProxySuite.ModulesEnabled.Home"))
            main.getHomeHandler().updateHomesFromDatabase(p);

        main.getProxy().getScheduler().schedule(main, new Runnable() {
            public void run() {
                if (main.getConfig().getBoolean("ProxySuite.ModulesEnabled.BanKickWarn")) {
                    main.getProxy().getScheduler().runAsync(main, new Runnable() {
                        public void run() {
                            try {
                                {
                                    ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT id FROM " + main.getTablePrefix() + "warnings " +
                                            "WHERE player = '" + p.getUniqueId() + "' AND archived = '0' AND " +
                                            "(UNIX_TIMESTAMP(date) + " + (main.getConfig().getInt("ProxySuite.Warnings" +
                                            ".TimeUntilArchive") * 24 * 60 * 60) + ") <= UNIX_TIMESTAMP(now())");
                                    while (rs.next()) {
                                        main.getWarningHandler().archiveWarn(rs.getInt("id"));
                                    }
                                }
                                if (main.getPermissionHandler().hasPermission(p, "proxysuite.messages.warnings.joininfo")) {
                                    {
                                        ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT * FROM " + main.getTablePrefix() + "warnings " +
                                                "WHERE player = '" + p.getUniqueId() + "' AND player_read = '0'");
                                        if (rs.next()) {
                                            main.getMessageHandler().sendMessage(p, main.getMessageHandler().getMessage
                                                    ("warning.unread"));
                                            rs.previous();
                                            while (rs.next()) {
                                                main.getMessageHandler().sendMessage(p, main.getMessageHandler().getMessage
                                                        ("warning.unread.entry").replace("%id%", "" + rs.getInt("id")).replace
                                                        ("%reason%", rs.getString("reason")));
                                            }
                                            main.getMessageHandler().sendMessage(p, main.getMessageHandler().getMessage("warning.unread.hideinfo"));
                                        }
                                    }
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                main.getProxy().getScheduler().schedule(main, new Runnable() {
                    public void run() {
                        main.getMessageHandler().broadcast(main.getMessageHandler().getMessage("join.broadcast").replace("%player%",
                                ev.getPlayer().getName()).replace("%player%", p.getName()).replace("%prefix%", main.getPlayerHandler
                                ().getPrefix(p)).replace("%suffix%", main.getPlayerHandler().getSuffix(p)));

                        if (main.getPermissionHandler().hasPermission(p, "proxysuite.messages.motd"))
                            for (String s : main.getConfig().getStringList("ProxySuite.Messages.MOTD"))
                                main.getMessageHandler().sendMessage(p, s);
                    }
                }, main.getConfig().getInt("ProxySuite.Messages.JoinMessageDelayMS"), TimeUnit.MILLISECONDS);

                if (main.getConfig().getBoolean("ProxySuite.ModulesEnabled.Vanish")) {
                    main.getProxy().getScheduler().runAsync(main, new Runnable() {
                        public void run() {
                            try {
                                ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT vanished FROM "
                                        + main.getTablePrefix() + "players WHERE uuid = '" + p.getUniqueId() + "'");
                                if (rs.next() && rs.getBoolean("vanished")) {
                                    main.getPlayerHandler().getVanishedPlayers().add(p);
                                    main.getPlayerHandler().sendVanishToServer(p);
                                    BungeeTabListPlus.hidePlayer(p);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                if (main.getConfig().getBoolean("ProxySuite.ModulesEnabled.Fly")) {
                    main.getProxy().getScheduler().runAsync(main, new Runnable() {
                        public void run() {
                            try {
                                ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT flying FROM " +
                                        main.getTablePrefix() + "players WHERE uuid = '" + p.getUniqueId() + "'");
                                if (rs.next() && rs.getBoolean("flying"))
                                    main.getPlayerHandler().getFlying().add(p);

                                if (main.getPlayerHandler().getFlying().contains(p))
                                    main.getPlayerHandler().sendFlyToServer(p);
                                else
                                    main.getPlayerHandler().sendUnflyToServer(p);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                if (main.getConfig().getBoolean("ProxySuite.ModulesEnabled.Gamemode")) {
                    main.getProxy().getScheduler().runAsync(main, new Runnable() {
                        public void run() {
                            try {
                                ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT gamemode FROM "
                                        + main.getTablePrefix() + "players WHERE uuid = '" + p.getUniqueId() + "'");
                                if (rs.next()) {
                                    main.getPlayerHandler().getGamemode().put(p, rs.getString("gamemode"));
                                    main.getPlayerHandler().sendGamemodeToServer(p, rs.getString("gamemode"));
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                main.getMessageHandler().getLastMessages().put(p, new LoggedMessage[main.getConfig().getInt("ProxySuite" +
                        ".Logging.LastMessagesLogged")]);

                String ip = ev.getPlayer().getPendingConnection().getAddress().getAddress().toString();
                int max = main.getConfig().getInt("ProxySuite.Security.IPJoinInfo");
                if (max > 0 && main.getPlayerHandler().getIps().containsKey(p) && main.getPlayerHandler().getIps().get(ip) >= max) {
                    String players = "";
                    for (ProxiedPlayer pl : main.getProxy().getPlayers())
                        if (pl != ev.getPlayer() && pl.getPendingConnection().getAddress().getAddress().toString().equals(ip))
                            players += pl.getName() + ", ";
                    if (players.length() > 1)
                        players = players.substring(0, players.length() - 2);
                    main.getMessageHandler().sendMessageWithPermission(main.getMessageHandler().getMessage("security.join" +
                            ".manyclients.info").replace("%player%", ev.getPlayer().getName()).replace("%sameIPList%",
                            players), "proxysuite.security.ipjoininfo");
                }
            }
        }, 500, TimeUnit.MILLISECONDS);
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent ev) {
        final ProxiedPlayer p = ev.getPlayer();
        final UUID uuid = p.getUniqueId();
        if (main.getConfig().getBoolean("ProxySuite.ModulesEnabled.Home"))
            main.getHomeHandler().removeHomesFromCache(p);
        main.getPlayerHandler().registerLogout(p);

        main.getMessageHandler().broadcast(main.getMessageHandler().getMessage("leave.broadcast").replace("%player%",
                p.getName()).replace("%prefix%", main.getPlayerHandler().getPrefix(p)).replace("%suffix%", main
                .getPlayerHandler().getSuffix(p)));

        main.getPlayerHandler().getVanishedPlayers().remove(p);
        main.getPlayerHandler().getFlying().remove(p);
        main.getPlayerHandler().getGamemode().remove(p);
        String ip = ev.getPlayer().getPendingConnection().getAddress().getAddress().toString();
        if (main.getPlayerHandler().getIps().get(ip) == 1)
            main.getPlayerHandler().getIps().remove(ip);
        else
            main.getPlayerHandler().getIps().put(ip, main.getPlayerHandler().getIps().get(ip) - 1);

        main.getPermissionHandler().resetPermissions(ev.getPlayer());
        //TODO Bei Bukkit Shutdown Nachricht zum IP aus Liste entfernen senden

        main.getProxy().getScheduler().schedule(main, new Runnable() {
            public void run() {
                main.getPlayerHandler().getPrefixes().remove(p);
                main.getPlayerHandler().getSuffixes().remove(p);
                if (p.getServer() == null)
                    main.getMessageHandler().getLastMessages().remove(p);
            }
        }, 60, TimeUnit.SECONDS);
    }

    @EventHandler
    public void onChat(ChatEvent ev) {
        if (main.getConfig().getBoolean("ProxySuite.Logging.CommandOutputEnabled"))
            if (ev.getMessage().startsWith("/") && ev.getSender() instanceof ProxiedPlayer)
                main.getLogger().info((ProxiedPlayer) ev.getSender() + " issued command: " + ev.getMessage());
            else if (ev.getSender() instanceof ProxiedPlayer)
                main.getMessageHandler().logMessage((ProxiedPlayer) ev.getSender(), ev.getMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent ev) {
        if (ev.getSuggestions().isEmpty()) {
            String partialPlayerName = ev.getCursor().toLowerCase();

            int lastSpaceIndex = partialPlayerName.lastIndexOf(' ');
            if (lastSpaceIndex >= 0) {
                partialPlayerName = partialPlayerName.substring(lastSpaceIndex + 1);
            }

            for (ProxiedPlayer p : main.getProxy().getPlayers()) {
                if (p.getName().toLowerCase().startsWith(partialPlayerName)) {
                    ev.getSuggestions().add(p.getName());
                }
            }
        }
    }
}
