package de.sabbertran.proxysuite.utils;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Home {
    private ProxiedPlayer player;
    private String name;
    private Location location;

    public Home(ProxiedPlayer player, String name, Location location) {
        this.player = player;
        this.name = name;
        this.location = location;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
