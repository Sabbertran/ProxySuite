package de.sabbertran.proxysuite.handlers;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.objects.Location;
import de.sabbertran.proxysuite.objects.Portal;
import de.sabbertran.proxysuite.objects.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PortalHandler {
    private ProxySuite main;
    private ArrayList<Portal> portals;
    private List<String> validTypes;

    public PortalHandler(ProxySuite main) {
        this.main = main;
        portals = new ArrayList<Portal>();
        validTypes = Arrays.asList(new String[]{"NOTHING", "NORMAL", "WATER", "NETHER"});
    }

    public Portal getPortal(String name) {
        for (Portal p : portals) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public void addPortal(ProxiedPlayer p, String name, String type, Warp destination) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("SetPortal");
            out.writeUTF(p.getServer().getInfo().getName());
            out.writeUTF(p.getName());
            out.writeUTF(name);
            out.writeUTF(type);
            out.writeUTF(destination.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.getServer().sendData("ProxySuite", b.toByteArray());
    }

    public void addPortalSuccess(final Portal p) {
        String sql;
        Portal old = getPortal(p.getName());
        if (old != null) {
            portals.remove(old);
            sql = "UPDATE " + main.getTablePrefix() + "portals SET `name` = '" + p.getName() + "', type = '" + p
                    .getType() + "', `server` = '" + p.getLoc1().getServer().getName() + "', " + "`world` = '" + p
                    .getLoc1().getWorld() + "', `loc1_x` = '" + p.getLoc1().getX() + "', `loc1_y` = '" + p.getLoc1()
                    .getY() + "', `loc1_z` = '" + p.getLoc1().getZ() + "', `loc1_pitch` = '" + p.getLoc1().getPitch()
                    + "', `loc1_yaw` = '" + p.getLoc1().getYaw() + "', loc2_x = '" + p.getLoc2().getX() + "', loc2_y " +
                    "= '" + p.getLoc2().getY() + "', loc2_z = '" + p.getLoc2().getZ() + "', loc2_pitch = '" + p
                    .getLoc2().getPitch() + "', loc2_yaw = '" + p.getLoc2().getYaw() + "', destination = '" + p
                    .getDestination() + "' WHERE LOWER(name) = '" + p.getName().toLowerCase() + "'";
        } else {
            sql = "INSERT INTO " + main.getTablePrefix() + "portals (name, type, server, world, loc1_x, loc1_y, " +
                    "loc1_z, loc1_pitch, loc1_yaw, loc2_x, loc2_y, loc2_z, loc2_pitch, loc2_yaw, destination) VALUES " +
                    "('" + p.getName() + "', '" + p.getType() + "', '" + p.getLoc1().getServer().getName() + "', '" +
                    p.getLoc1().getWorld() + "', '" + p.getLoc1().getX() + "', '" + p.getLoc1().getY() + "', '" + p
                    .getLoc1().getZ() + "', '" + p.getLoc1().getPitch() + "', '" + p.getLoc1().getYaw() + "', '" + p
                    .getLoc2().getX() + "', '" + p.getLoc2().getY() + "', '" + p.getLoc2().getZ() + "', '" + p
                    .getLoc2().getPitch() + "', '" + p.getLoc1().getYaw() + "', '" + p.getDestination() + "')";
        }
        portals.add(p);

        final String sql2 = sql;
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    PreparedStatement st = main.getSQLConnection().prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS);
                    st.execute();
                    p.setId(st.getGeneratedKeys().getInt(1));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void readPortalsFromDatabase() {
        portals.clear();
        try {
            ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT * FROM " + main
                    .getTablePrefix() + "portals");
            while (rs.next()) {
                Location loc1 = new Location(main.getProxy().getServerInfo(rs.getString("server")), rs.getString
                        ("world"), rs.getDouble("loc1_x"), rs.getDouble("loc1_y"), rs.getDouble("loc1_z"), rs
                        .getFloat("loc1_pitch"), rs.getFloat("loc1_yaw"));
                Location loc2 = new Location(main.getProxy().getServerInfo(rs.getString("server")), rs.getString
                        ("world"), rs.getDouble("loc2_x"), rs.getDouble("loc2_y"), rs.getDouble("loc2_z"), rs
                        .getFloat("loc2_pitch"), rs.getFloat("loc2_yaw"));
                Warp destination = main.getWarpHandler().getWarp(rs.getString("destination"), true);
                if (destination != null) {
                    Portal p = new Portal(rs.getInt("id"), rs.getString("name"), rs.getString("type"), loc1, loc2, destination.getName());
                    portals.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void sendPortalList(CommandSender sender) {
        main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("portal.list.header"));
        String message = "[";
        for (Portal p : portals) {
            String entry;
            Warp destination = main.getWarpHandler().getWarp(p.getDestination(), true);
            if (destination != null)
                entry = main.getMessageHandler().getMessage("portal.list.entry").replace("%portal%", p.getName())
                        .replace("%destination%", p.getDestination()).replace("%destinationServer%", destination
                                .getLocation().getServer().getName()).replace("%destinationWorld%", destination
                                .getLocation().getWorld()).replace("%destinationCoordX%", "" + destination.getLocation
                                ().getXInt()).replace("%destinationCoordY%", "" + destination.getLocation().getYInt())
                        .replace("%destinationCoordZ%", "" + destination.getLocation().getZInt());
            else
                entry = main.getMessageHandler().getMessage("portal.list.entry.nodestination").replace("%portal%",
                        p.getName());
            if ((entry.startsWith("{") && entry.endsWith("}")) || (entry.startsWith("[") && entry
                    .endsWith("]")))
                entry += ",{\"text\":\", \"},";
            else
                entry += ", ";
            message += entry;
        }
        if (message.endsWith(", "))
            message = message.substring(0, message.length() - 2);
        else if (message.endsWith(",{\"text\":\", \"},"))
            message = message.substring(0, message.length() - 15);
        message += "]";
        main.getMessageHandler().sendMessage(sender, message);
        main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("portal.list.footer"));
    }

    public void sendPortalsToServer(ServerInfo s) {
        for (Portal p : portals) {
            if (p.getLoc1().getServer() == s) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                try {
                    out.writeUTF("Portal");
                    out.writeUTF(p.getName());
                    out.writeUTF(p.getLoc1().getWorld());
                    out.writeUTF("" + p.getLoc1().getX());
                    out.writeUTF("" + p.getLoc1().getY());
                    out.writeUTF("" + p.getLoc1().getZ());
                    out.writeUTF("" + p.getLoc2().getX());
                    out.writeUTF("" + p.getLoc2().getY());
                    out.writeUTF("" + p.getLoc2().getZ());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                s.sendData("ProxySuite", b.toByteArray());
            }
        }
    }

    public void deletePortal(final Portal p) {
        portals.remove(p);
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("DeletePortal");
            out.writeUTF(p.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.getLoc1().getServer().sendData("ProxySuite", b.toByteArray());

        final String sql = "DELETE FROM " + main.getTablePrefix() + "portals WHERE id = ?";
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    PreparedStatement pst = main.getSQLConnection().prepareCall(sql);
                    pst.setInt(1, p.getId());
                    pst.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public List<String> getValidTypes() {
        return validTypes;
    }
}
