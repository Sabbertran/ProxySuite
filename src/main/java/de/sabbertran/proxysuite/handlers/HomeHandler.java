package de.sabbertran.proxysuite.handlers;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.utils.Home;
import de.sabbertran.proxysuite.utils.Location;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class HomeHandler {

    private ProxySuite main;
    private HashMap<ProxiedPlayer, ArrayList<Home>> homes;

    public HomeHandler(ProxySuite main) {
        this.main = main;
        homes = new HashMap<ProxiedPlayer, ArrayList<Home>>();
    }

    /**
     * ACHTUNG!!! Datenbankzugriff wird nicht asynchron ausgeführt! Methode nur aus asynchronem Kontext aufrufen wenn
     * der player nicht online ist
     *
     * @param player .
     * @param name   .
     * @return .
     */
    public Home getHome(String player, String name) {
        ProxiedPlayer p = main.getPlayerHandler().getPlayer(player, null, false);
        if (p != null && homes.containsKey(p)) {
            for (Home h : homes.get(p)) {
                if (h.getName().equalsIgnoreCase(name)) {
                    return h;
                }
            }
        } else {
            try {
                ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT " + main.getTablePrefix
                        () + "homes.* FROM " + main.getTablePrefix() + "homes, " + main.getTablePrefix() + "players " +
                        "WHERE " + main.getTablePrefix() + "homes.player = " + main.getTablePrefix() + "players.uuid"
                        + " AND " + main.getTablePrefix() + "players.name = '" + player + "' AND " + main
                        .getTablePrefix() + "homes.name = '" + name + "'");
                if (rs.next()) {
                    Location loc = new Location(main.getProxy().getServerInfo(rs.getString("server")), rs.getString
                            ("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("pitch"), rs.getFloat("yaw"));
                    Home h = new Home(p, name, loc);
                    return h;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setHome(String player, String name, Location loc) {
        String sql;
        ProxiedPlayer p = main.getPlayerHandler().getPlayer(player, null, false);
        Home old = p != null ? getHome(p.getName(), name) : null;

        String sqlDel = "";

        if (old != null) {
            old.setName(name);
            old.setLocation(loc);
            sql = "UPDATE " + main.getTablePrefix() + "homes SET `name` = '" + name + "', `server` = '" + loc
                    .getServer().getName() + "', " + "`world` = '" + loc.getWorld() + "'," + " `x` = '" + loc.getX()
                    + "', `y` = '" + loc.getY() + "', `z` = '" + loc.getZ() + "', `pitch` = '" + loc.getPitch() + "'," +
                    " `yaw` = '" + loc.getYaw() + "' WHERE player = '" + p.getUniqueId() + "' AND LOWER(name) = '" +
                    name.toLowerCase() + "'";
        } else {
            if (p != null) {
                Home h = new Home(p, name, loc);
                homes.get(p).add(h);
            }

            sqlDel = "DELETE FROM " + main.getTablePrefix() + "homes WHERE LOWER(name) = '" + name.toLowerCase() + "'" +
                    " AND player IN (SELECT uuid FROM " + main.getTablePrefix() + "players WHERE name = '" + p
                    .getName() + "')";

            sql = "INSERT INTO " + main.getTablePrefix() + "homes (player, name, server, world, x, y, z, pitch, yaw) " +
                    "SELECT " + main.getTablePrefix() + "players.uuid, '" + name + "', '" + loc.getServer().getName()
                    + "', '" + loc.getWorld() + "', '" + loc.getX() + "', '" + loc.getY() + "', '" + loc.getZ() + "'," +
                    " '" + loc.getPitch() + "', '" + loc.getYaw() + "' FROM " + main.getTablePrefix() + "players " +
                    "WHERE LOWER(" + main.getTablePrefix() + "players.name) = '" + player.toLowerCase() + "'";
        }

        final String sql2 = sql;
        final String sqlDel2 = sqlDel;
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    if (!sqlDel2.equals(""))
                        main.getSQLConnection().createStatement().execute(sqlDel2);
                    main.getSQLConnection().createStatement().execute(sql2);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void deleteHome(final String player, final String name) {
        ProxiedPlayer p = main.getPlayerHandler().getPlayer(player, null, false);
        if (p != null) {
            for (Home h : homes.get(p)) {
                if (h.getName().equalsIgnoreCase(name)) {
                    homes.get(p).remove(h);
                    break;
                }
            }
        }

        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                String sql = "DELETE FROM " + main.getTablePrefix() + "homes WHERE LOWER(name) = '" + name
                        .toLowerCase() + "' AND player IN (SELECT uuid FROM " + main.getTablePrefix() + "players " +
                        "WHERE name = '" + player + "')";
                try {
                    main.getSQLConnection().createStatement().execute(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public int getHomesInWorld(ProxiedPlayer p, ServerInfo server, String world) {
        int ret = 0;
        for (Home h : homes.get(p))
            if (h.getLocation().getServer().equals(server) && h.getLocation().getWorld().equals(world))
                ret++;
        return ret;
    }

    public void updateHomesFromDatabase(final ProxiedPlayer p) {
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                homes.remove(p);
                ArrayList<Home> insert = new ArrayList<Home>();
                try {
                    ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT * FROM " + main
                            .getTablePrefix() + "homes WHERE player = '" + p.getUniqueId() + "'");
                    while (rs.next()) {
                        String name = rs.getString("name");
                        Location loc = new Location(main.getProxy().getServerInfo(rs.getString("server")), rs
                                .getString("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs
                                .getFloat("pitch"), rs.getFloat("yaw"));
                        Home h = new Home(p, name, loc);
                        insert.add(h);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                homes.put(p, insert);
            }
        });
    }

    public void sendHomeList(final CommandSender sender, final String player) {
        final ArrayList<Home> homes = new ArrayList<Home>();
        homes.add(new Home(null, null, null));
        ProxiedPlayer p = main.getPlayerHandler().getPlayer(player, sender, false);
        if (p != null && this.homes.containsKey(p)) {
            homes.clear();
            for (Home h : this.homes.get(p)) {
                homes.add(h);
            }
        } else {
            main.getProxy().getScheduler().runAsync(main, new Runnable() {
                public void run() {
                    try {
                        ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT " + main
                                .getTablePrefix() + "homes.* FROM " + main.getTablePrefix() + "homes, " + main
                                .getTablePrefix() + "players WHERE " + main.getTablePrefix() + "homes.player = " +
                                main.getTablePrefix() + "players.uuid AND " + main.getTablePrefix() + "players.name =" +
                                " '" + player + "'");
                        homes.clear();
                        while (rs.next()) {
                            String name = rs.getString("name");
                            Location loc = new Location(main.getProxy().getServerInfo(rs.getString("server")), rs.getString
                                    ("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("pitch"), rs.getFloat("yaw"));
                            Home h = new Home(null, name, loc);
                            homes.add(h);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }


        final ScheduledTask[] task = new ScheduledTask[1];
        task[0] = main.getProxy().getScheduler().schedule(main, new Runnable() {
            public void run() {
                if (homes.size() != 1 || (homes.get(0).getPlayer() != null && homes.get(0).getName() != null &&
                        homes.get(0).getLocation() != null)) {
                    main.getProxy().getScheduler().cancel(task[0]);
                    String message;
                    if (homes.size() != 0) {
                        if (sender.getName() == player)
                            message = main.getMessageHandler().getMessage("home.list.header");
                        else
                            message = main.getMessageHandler().getMessage("home.list.header.others").replace("%player%",
                                    player);
                        main.getMessageHandler().sendMessage(sender, message);
                    }
                    String homeList;
                    if (sender instanceof ProxiedPlayer) {
                        ProxiedPlayer p = (ProxiedPlayer) sender;
                        homeList = "[";
                        for (Home h : homes) {
                            String entry;
                            if (main.getPermissionHandler().hasPermission(sender, "proxysuite.homes.showcoordinates")) {
                                if (sender.getName() == player)
                                    entry = main.getMessageHandler().getMessage("home.list.entry.withlocation")
                                            .replace("%home%", h.getName()).replace("%server%", h.getLocation()
                                                    .getServer().getName()).replace("%world%", h.getLocation()
                                                    .getWorld()).replace("%coordX%", "" + h.getLocation().getXInt())
                                            .replace("%coordY%", "" + h.getLocation().getYInt()).replace("%coordZ%",
                                                    "" + h.getLocation().getZInt());
                                else
                                    entry = main.getMessageHandler().getMessage("home.list.entry.withlocation.others")
                                            .replace("%home%", h.getName()).replace("%server%", h.getLocation()
                                                    .getServer().getName()).replace("%world%", h.getLocation()
                                                    .getWorld()).replace("%coordX%", "" + h.getLocation().getXInt())
                                            .replace("%coordY%", "" + h.getLocation().getYInt()).replace("%coordZ%",
                                                    "" + h.getLocation().getZInt()).replace("%player%", player);
                            } else {
                                if (sender.getName() == player)
                                    entry = main.getMessageHandler().getMessage("home.list.entry").replace("%home%",
                                            h.getName());
                                else
                                    entry = main.getMessageHandler().getMessage("home.list.entry.others").replace
                                            ("%home%", h.getName()).replace("%player%", player);
                            }
                            if ((entry.startsWith("{") && entry.endsWith("}")) || (entry.startsWith("[") && entry
                                    .endsWith("]")))
                                entry += ",{\"text\":\", \"},";
                            else
                                entry += ", ";
                            homeList += entry;
                        }
                        if (homeList.endsWith(", "))
                            homeList = homeList.substring(0, homeList.length() - 2);
                        else if (homeList.endsWith(",{\"text\":\", \"},"))
                            homeList = homeList.substring(0, homeList.length() - 15);

                        homeList += "]";

                        if (homes.size() == 0)
                            homeList = main.getMessageHandler().getMessage("home.list.nofound");

                        main.getMessageHandler().sendMessage(sender, homeList);
                        message = main.getMessageHandler().getMessage("home.list.footer");
                    } else {
                        homeList = "";
                        for (Home h : homes) {
                            homeList = homeList + h.getName() + ", ";
                        }
                        if (homeList.length() > 0) {
                            homeList = homeList.substring(0, homeList.length() - 2);
                        }
                        sender.sendMessage(new TextComponent(homeList));
                    }
                }
            }
        }, 10, 500, TimeUnit.MILLISECONDS);
    }

    public int getMaximumHomes(String player) {
        int highest = main.getConfig().getInt("ProxySuite.Homes.DefaultMaximum");
        if (main.getPermissionHandler().getPermissions().containsKey(player)) {
            if (main.getPermissionHandler().hasPermission(player, "proxysuite.commands.sethome.multiple.*"))
                return -1;
            for (String s : main.getPermissionHandler().getPermissions().get(player))
                if (s.startsWith("proxysuite.commands.sethome.multiple.")) {
                    String amount = s.replace("proxysuite.commands.sethome.multiple.", "");
                    try {
                        int temp = Integer.parseInt(amount);
                        if (temp > highest)
                            highest = temp;
                    } catch (NumberFormatException ex) {
                    }
                }
        }
        return highest;
    }

    public int getMaximumHomesPerWorld(String player) {
        int highest = main.getConfig().getInt("ProxySuite.Homes.DefaultMaximumPerWorld");
        if (main.getPermissionHandler().getPermissions().containsKey(player)) {
            if (main.getPermissionHandler().hasPermission(player, "proxysuite.commands.sethome.world.multiple.*"))
                return -1;
            for (String s : main.getPermissionHandler().getPermissions().get(player))
                if (s.startsWith("proxysuite.commands.sethome.world.multiple.")) {
                    String amount = s.replace("proxysuite.commands.sethome.world.multiple.", "");
                    try {
                        int temp = Integer.parseInt(amount);
                        if (temp > highest)
                            highest = temp;
                    } catch (NumberFormatException ex) {
                    }
                }
        }
        return highest;
    }

    public int getHomeAmount(ProxiedPlayer p) {
        return homes.containsKey(p) ? homes.get(p).size() : 0;
    }

    public void removeHomesFromCache(ProxiedPlayer p) {
        homes.remove(p);
    }
}
