package de.sabbertran.proxysuite.objects;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Date;

public class LoggedMessage {

    private String message;
    private ProxiedPlayer sender;
    private Date date;

    public LoggedMessage(ProxiedPlayer sender, String message, Date date) {
        this.message = message;
        this.sender = sender;
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public ProxiedPlayer getSender() {
        return sender;
    }

    public Date getDate() {
        return date;
    }
}
