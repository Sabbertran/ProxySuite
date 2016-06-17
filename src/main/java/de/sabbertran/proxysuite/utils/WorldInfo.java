package de.sabbertran.proxysuite.utils;

public class WorldInfo {
    private String world;
    private long time;

    public WorldInfo(String world, long time) {
        this.world = world;
        this.time = time;
    }

    public String getWorld() {
        return world;
    }

    public long getTime() {
        return time;
    }
}
