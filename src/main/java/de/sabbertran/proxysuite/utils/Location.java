package de.sabbertran.proxysuite.utils;

import net.md_5.bungee.api.config.ServerInfo;

public class Location {

    private ServerInfo server;
    private String world;
    private double x, y, z;
    private float pitch, yaw;

    public Location(ServerInfo server, String world) {
        this.server = server;
        this.world = world;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.pitch = 0;
        this.yaw = 0;
    }

    public Location(ServerInfo server, String world, double x, double y, double z) {
        this.server = server;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = 0;
        this.yaw = 0;
    }

    public Location(ServerInfo server, String world, double x, double y, double z, float pitch, float yaw) {
        this.server = server;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    @Override
    public String toString() {
        return "Location{" +
                "server=" + server.getName() +
                ", world='" + world + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", pitch=" + pitch +
                ", yaw=" + yaw +
                '}';
    }

    public ServerInfo getServer() {
        return server;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getXInt() {
        return (int) Math.floor(x);
    }

    public int getYInt() {
        return (int) Math.floor(y);
    }

    public int getZInt() {
        return (int) Math.floor(z);
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }
}
