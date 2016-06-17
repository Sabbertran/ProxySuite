package de.sabbertran.proxysuite.handlers;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.utils.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SpawnHandler {

    private ProxySuite main;
    private Location firstSpawn, normalSpawn;

    public SpawnHandler(ProxySuite main) {
        this.main = main;
    }

    public void setFirstSpawn(Location loc) {
        String sql;
        if (firstSpawn != null) {
            sql = "UPDATE " + main.getTablePrefix() + "spawns SET server = '" + loc.getServer()
                    .getName() + "', " + "world = '" + loc.getWorld() + "'," + " x = '" + loc.getX() + "', y = " +
                    "'" + loc.getY() + "', z = '" + loc.getZ() + "', pitch = '" + loc.getPitch() + "', yaw = '"
                    + loc.getYaw() + "' WHERE type = 'FIRST'";
        } else {
            sql = "INSERT INTO " + main.getTablePrefix() + "spawns (type, server, world, x, y, z, pitch, yaw) VALUES " +
                    "('FIRST', '" + loc.getServer().getName() + "', '" + loc.getWorld() + "', '" + loc.getX() + "', "
                    + "'" + loc.getY() + "', '" + loc.getZ() + "', '" + loc.getPitch() + "', '" + loc.getYaw() + "')" +
                    "";
        }

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
        firstSpawn = loc;
    }

    public void setNormalSpawn(Location loc) {
        String sql;
        if (normalSpawn != null) {
            sql = "UPDATE " + main.getTablePrefix() + "spawns SET server = '" + loc.getServer()
                    .getName() + "', world = '" + loc.getWorld() + "'," + " x = '" + loc.getX() + "', y = " +
                    "'" + loc.getY() + "', z = '" + loc.getZ() + "', pitch = '" + loc.getPitch() + "', yaw = '"
                    + loc.getYaw() + "' WHERE type = 'NORMAL'";
        } else {
            sql = "INSERT INTO " + main.getTablePrefix() + "spawns (type, server, world, x, y, z, pitch, yaw) VALUES " +
                    "('NORMAL', '" + loc.getServer().getName() + "', '" + loc.getWorld() + "', '" + loc.getX() + "', " +
                    "" + "'" + loc.getY() + "', '" + loc.getZ() + "', '" + loc.getPitch() + "', '" + loc.getYaw() +
                    "')";
        }

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
        normalSpawn = loc;
    }

    public void readSpawnsFromDatabase() {
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT * FROM " + main
                            .getTablePrefix() + "spawns");
                    while (rs.next()) {
                        Location loc = new Location(main.getProxy().getServerInfo(rs.getString("server")), rs
                                .getString("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("pitch"), rs.getFloat("yaw"));
                        if (rs.getString("type").equals("NORMAL"))
                            normalSpawn = loc;
                        else if (rs.getString("type").equals("FIRST"))
                            firstSpawn = loc;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Location getNormalSpawn() {
        return normalSpawn;
    }

    public Location getFirstSpawn() {
        return firstSpawn;
    }
}
