package de.sabbertran.proxysuite.objects;

public class Warp {
    private String name;
    private boolean hidden;
    private Location location;

    public Warp(String name, Location location, boolean hidden) {
        this.name = name;
        this.hidden = hidden;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public boolean isHidden() {
        return hidden;
    }

    public Location getLocation() {
        return location;
    }
}
