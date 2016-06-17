package de.sabbertran.proxysuite;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ProxySuiteAPI {
    public static boolean isVanished(ProxiedPlayer p) {
        return ProxySuite.getInstance().getPlayerHandler().getVanishedPlayers().contains(p);
    }

    public static String getPrefix(ProxiedPlayer p) {
        return ProxySuite.getInstance().getPlayerHandler().getPrefix(p);
    }

    public static String getSuffix(ProxiedPlayer p) {
        return ProxySuite.getInstance().getPlayerHandler().getSuffix(p);
    }
}
