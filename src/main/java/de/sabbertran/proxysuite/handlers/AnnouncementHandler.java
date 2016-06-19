package de.sabbertran.proxysuite.handlers;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AnnouncementHandler {
    private ProxySuite main;
    private ArrayList<String> announcements;
    private int currentAnnouncement;
    private ScheduledTask task;

    public AnnouncementHandler(ProxySuite main) {
        this.main = main;
        announcements = new ArrayList<String>();
        currentAnnouncement = -1;
    }

    public void readAnnouncementsFromFile() {
        announcements = new ArrayList<String>();
        File f = new File(main.getDataFolder(), "announcements.yml");
        try {
            BufferedReader read = new BufferedReader(new FileReader(f));
            String line;
            while ((line = read.readLine()) != null) {
                line = line.trim();
                if (line.equals("''"))
                    announcements.add("");
                else if (!line.equals("") && !line.startsWith("#"))
                    announcements.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentAnnouncement = 0;
    }

    public void startScheduler() {
        int interval = main.getConfig().getInt("ProxySuite.Announcements.Interval");
        task = main.getProxy().getScheduler().schedule(main, new Runnable() {
            public void run() {
                broadcastMessage();
            }
        }, interval, interval, TimeUnit.SECONDS);
    }

    private void broadcastMessage() {
        if (!announcements.isEmpty() && main.getProxy().getPlayers().size() > 0) {
            String message;
            if (!main.getConfig().getBoolean("ProxySuite.Announcements.Random")) {
                message = announcements.get(currentAnnouncement);
                currentAnnouncement++;
                if (currentAnnouncement == announcements.size())
                    currentAnnouncement = 0;
            } else {
                Random r = new Random();
                message = announcements.get(r.nextInt(announcements.size()));
            }

            if (!message.trim().equals("")) {
                if (message.startsWith("[") && message.endsWith("]"))
                    message = "[" + main.getConfig().getString("ProxySuite.Announcements.PrefixJson") + "," +
                            message + "]";
                else
                    message = main.getConfig().getString("ProxySuite.Announcements.Prefix") + message;

                main.getMessageHandler().broadcast(message);
            }
        }
    }
}
