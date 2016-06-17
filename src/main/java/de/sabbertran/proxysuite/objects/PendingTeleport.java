package de.sabbertran.proxysuite.objects;

import de.sabbertran.proxysuite.handlers.TeleportHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

public class PendingTeleport {

    private TeleportHandler handler;
    private TeleportType type;
    private ProxiedPlayer from, to;
    private ScheduledTask task;

    public PendingTeleport(TeleportHandler handler, TeleportType type, ProxiedPlayer from, ProxiedPlayer to, int delay) {
        this.handler = handler;
        this.type = type;
        this.from = from;
        this.to = to;
        scheduleCancel(delay);
    }

    public enum TeleportType {
        TPA, TPAHERE
    }

    private void scheduleCancel(int delay) {
        task = handler.getMain().getProxy().getScheduler().schedule(handler.getMain(), new Runnable() {
            public void run() {
                cancel();
            }
        }, delay, TimeUnit.SECONDS);
    }

    public void cancel() {
        if (type == TeleportType.TPA) {
            if (from.getServer() != null)
                handler.getMain().getMessageHandler().sendMessage(from, handler.getMain().getMessageHandler().getMessage
                        ("teleport.request.timeout").replace("%player%", to.getName()).replace("%prefix%", handler.getMain()
                        .getPlayerHandler().getPrefix(to)).replace("%suffix%", handler.getMain()
                        .getPlayerHandler().getSuffix(to)));
            if (to.getServer() != null)
                handler.getMain().getMessageHandler().sendMessage(to, handler.getMain().getMessageHandler().getMessage
                        ("teleport.request.timeout.other").replace("%player%", from.getName()).replace("%prefix%", handler.getMain()
                        .getPlayerHandler().getPrefix(from)).replace("%suffix%", handler.getMain()
                        .getPlayerHandler().getSuffix(from)));
        } else if (type == TeleportType.TPAHERE) {
            if (from.getServer() != null)
                handler.getMain().getMessageHandler().sendMessage(from, handler.getMain().getMessageHandler().getMessage
                        ("teleport.request.timeout.other").replace("%player%", to.getName()).replace("%prefix%", handler.getMain()
                        .getPlayerHandler().getPrefix(to)).replace("%suffix%", handler.getMain()
                        .getPlayerHandler().getSuffix(to)));
            if (to.getServer() != null)
                handler.getMain().getMessageHandler().sendMessage(to, handler.getMain().getMessageHandler().getMessage
                        ("teleport.request.timeout").replace("%player%", from.getName()).replace("%prefix%", handler.getMain()
                        .getPlayerHandler().getPrefix(from)).replace("%suffix%", handler.getMain()
                        .getPlayerHandler().getSuffix(from)));
        }
        handler.getPendingTeleports().remove(this);
    }

    public void cancelCancel() {
        if (task != null) {
            handler.getMain().getProxy().getScheduler().cancel(task);
            task = null;
        }
    }

    public ProxiedPlayer getTo() {
        return to;
    }

    public ProxiedPlayer getFrom() {
        return from;
    }

    public TeleportType getType() {
        return type;
    }
}
