package de.sabbertran.proxysuite.handlers;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.objects.Location;
import de.sabbertran.proxysuite.objects.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class WarpHandler {

    private ProxySuite main;
    private ArrayList<Warp> warps;

    public WarpHandler(ProxySuite main) {
        this.main = main;
        warps = new ArrayList<Warp>();
    }

    public void readWarpsFromDatabase() {
        warps.clear();
        try {
            ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT * FROM " + main
                    .getTablePrefix() + "warps");
            while (rs.next()) {
                Warp w = new Warp(rs.getString("name"), new Location(main.getProxy()
                        .getServerInfo(rs
                                .getString("server")), rs.getString("world"), rs.getDouble("x"), rs.getDouble("y"), rs
                        .getDouble("z"), rs.getFloat("pitch"), rs.getFloat("yaw")), rs.getBoolean("hidden"));
                warps.add(w);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void setWarp(String name, Location loc, boolean hidden) {
        String sql;
        Warp old = getWarp(name, true);
        if (old != null) {
            warps.remove(old);
            sql = "UPDATE " + main.getTablePrefix() + "warps SET `name` = '" + name + "', `hidden` = '" + (hidden ? 1
                    : 0) + "', `server` = '" + loc.getServer().getName() + "', " + "`world` = '" + loc.getWorld() +
                    "'," + " `x` = '" + loc.getX() + "', `y` = '" + loc.getY() + "', `z` = '" + loc.getZ() + "', " +
                    "`pitch` = '" + loc.getPitch() + "', `yaw` = '" + loc.getYaw() + "' WHERE LOWER(name) = '" + name
                    .toLowerCase() + "'";
        } else {
            sql = "INSERT INTO " + main.getTablePrefix() + "warps (name, hidden, server, world, x, y, z, pitch, yaw) " +
                    "VALUES ('" + name + "', '" + (hidden ? 1 : 0) + "', '" + loc.getServer().getName() + "', '" +
                    loc.getWorld() + "', '" + loc.getX() + "', " + "'" + loc.getY() + "', '" + loc.getZ() + "', '" +
                    loc.getPitch() + "', '" + loc.getYaw() + "')";
        }
        Warp w = new Warp(name, loc, hidden);
        warps.add(w);

        final String sql2 = sql;
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    main.getSQLConnection().createStatement().execute(sql2);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void deleteWarp(Warp w) {
        final String sql = "DELETE FROM " + main.getTablePrefix() + "warps WHERE `name` = '" + w.getName() + "'";
        warps.remove(w);
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    main.getSQLConnection().createStatement().execute(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendWarpList(CommandSender sender, boolean includeHidden) {
        main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("warp.list.header"));
        String warps;
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            warps = "[";
            for (Warp w : this.warps) {
                if (!w.isHidden() || includeHidden) {
                    String entry;
                    if (main.getPermissionHandler().hasPermission(sender, "proxysuite.warps.showcoordinates")) {
                        if (!w.isHidden())
                            entry = main.getMessageHandler().getMessage("warp.list.entry.withlocation");
                        else
                            entry = main.getMessageHandler().getMessage("warp.list.entry.hidden.withlocation");
                    } else {
                        if (!w.isHidden())
                            entry = main.getMessageHandler().getMessage("warp.list.entry");
                        else
                            entry = main.getMessageHandler().getMessage("warp.list.entry.hidden");
                    }
                    entry = entry.replace("%warp%", w.getName()).replace("%server%", w.getLocation().getServer()
                            .getName()).replace("%world%", w.getLocation().getWorld()).replace("%coordX%", "" + w
                            .getLocation().getXInt()).replace("%coordY%", "" + w.getLocation().getYInt()).replace
                            ("%coordZ%", "" + w.getLocation().getZInt());
                    if ((entry.startsWith("{") && entry.endsWith("}")) || (entry.startsWith("[") && entry
                            .endsWith("]")))
                        entry += ",{\"text\":\", \"},";
                    else
                        entry += ", ";
                    warps += entry;
                }
            }
            if (warps.endsWith(", "))
                warps = warps.substring(0, warps.length() - 2);
            else if (warps.endsWith(",{\"text\":\", \"},"))
                warps = warps.substring(0, warps.length() - 15);
            warps += "]";

            if (warps.equals("[\"\",]"))
                warps = main.getMessageHandler().getMessage("warp.list.nofound");

            main.getMessageHandler().sendMessage(p, warps);
        } else {
            warps = "";
            for (Warp w : this.warps) {
                if (!w.isHidden() || includeHidden) {
                    warps = warps + w.getName() + ", ";
                }
            }
            if (warps.length() > 0) {
                warps = warps.substring(0, warps.length() - 2);
            }
            sender.sendMessage(new TextComponent(warps));
        }
        main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("warp.list.footer"));
    }

    public Warp getWarp(String name, boolean includeHidden) {
        if (name != null) {
            for (Warp w : warps) {
                if (w.getName().equalsIgnoreCase(name) && (!w.isHidden() || includeHidden)) {
                    return w;
                }
            }
        }
        return null;
    }
}
