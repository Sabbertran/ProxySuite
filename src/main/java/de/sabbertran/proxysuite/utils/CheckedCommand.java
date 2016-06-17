package de.sabbertran.proxysuite.utils;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CheckedCommand {
    private ProxiedPlayer player;
    private String command;
    private boolean canExecute;

    public CheckedCommand(ProxiedPlayer player, String command, boolean canExecute) {
        this.player = player;
        this.command = command;
        this.canExecute = canExecute;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    public String getCommand() {
        return command;
    }

    public boolean canExecute() {
        return canExecute;
    }
}
