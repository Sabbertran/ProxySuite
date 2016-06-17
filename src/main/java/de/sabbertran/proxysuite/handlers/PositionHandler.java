package de.sabbertran.proxysuite.handlers;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.objects.Location;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class PositionHandler {

    private ProxySuite main;
    private HashMap<UUID, Runnable> positionRunnables;
    private HashMap<UUID, Location> positions, localPositions;

    public PositionHandler(ProxySuite main) {
        this.main = main;
        positionRunnables = new HashMap<UUID, Runnable>();
        positions = new HashMap<UUID, Location>();
        localPositions = new HashMap<UUID, Location>();
    }

    public void requestPosition(ProxiedPlayer p) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("GetPosition");
            out.writeUTF(p.getName());
            out.writeUTF(p.getServer().getInfo().getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.getServer().sendData("ProxySuite", b.toByteArray());
    }

    public void locationReceived(ProxiedPlayer p, Location loc) {
        if (positionRunnables.containsKey(p.getUniqueId())) {
            localPositions.put(p.getUniqueId(), loc);
            positionRunnables.remove(p.getUniqueId()).run();
        } else
            positions.put(p.getUniqueId(), loc);
    }

    public HashMap<UUID, Location> getLocalPositions() {
        return localPositions;
    }

    public void addPositionRunnable(ProxiedPlayer p, Runnable run) {
        positionRunnables.put(p.getUniqueId(), run);
    }
}
