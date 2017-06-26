package de.sabbertran.proxysuite.handlers;

import de.sabbertran.proxysuite.ProxySuite;
import de.sabbertran.proxysuite.commands.CustomCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CustomCommandHandler {

    private ProxySuite main;

    public CustomCommandHandler(ProxySuite main) {
        this.main = main;
    }

    public void registerCustomCommandsFromFile() {
        File folder = new File(main.getDataFolder(), "/customcommands");
        if (!folder.exists())
            folder.mkdirs();
        for (File f : folder.listFiles()) {
            if (f.getName().toLowerCase().endsWith(".yml")) {
                try {
                    Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new InputStreamReader(new FileInputStream(f), "UTF8"));
                    String command = f.getName().replace(".yml", "").trim();
                    ArrayList<String> disabledServers = (ArrayList<String>) config.getStringList("DisabledServers");
                    String permission = config.getString("Permission");
                    main.getPermissionHandler().getAvailablePermissions().add(permission);
                    String[] messages = config.getStringList("Messages").toArray(new String[config.getStringList
                            ("Messages").size()]);

                    for (String msg : messages) {
                        if (msg.startsWith("%permission:")) {
                            String full = msg.substring(1, msg.length() - 1);
                            String perm = full.split(":")[1];
                            main.getPermissionHandler().getAvailablePermissions().add(perm);
                        }
                    }

                    CustomCommand cmd = new CustomCommand(main, command, permission, messages, disabledServers);
                    main.getProxy().getPluginManager().registerCommand(main, cmd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Wird nicht asynchron ausgeführt! Nur aus asynchronem Kontext ausführen
     *
     * @param input .
     * @param p     .
     * @return .
     */
    public String translateVariables(String input, final ProxiedPlayer p) {
        String output = "" + input;
        if (output.contains("%totalPlayers%")) {
            int totalPlayers = 0;
            try {
                ResultSet rs = main.getSQLConnection().createStatement().executeQuery("SELECT COUNT(*) AS count FROM " +
                        main.getTablePrefix() + "players");
                if (rs.next())
                    totalPlayers = rs.getInt("count");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            output = output.replace("%totalPlayers%", "" + totalPlayers);
        }
        if (output.contains("%world%") || output.contains("%worldTime%")) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            try {
                out.writeUTF("GetPlayerWorldInfo");
                out.writeUTF(p.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
            p.getServer().sendData("ProxySuite", b.toByteArray());

            int count = 0;
            while (!main.getPlayerHandler().getWorldInfos().containsKey(p) && count < 100) {
                try {
                    count++;
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (count != 100) {
                output = output.replace("%world%", main.getPlayerHandler().getWorldInfos().get(p).getWorld()) //
                        .replace("%worldTime%", new SimpleDateFormat("HH:mm").format(new Date(main.getPlayerHandler()
                                .getWorldInfos().get(p).getTime())));
                main.getPlayerHandler().getWorldInfos().remove(p);
            }
        }
        if (output.startsWith("%permission:")) {
            String full = output.substring(1, output.length() - 1);
            String permission = full.split(":")[1];
            String format = full.split(":")[2];
            String replace = "";
            for (ProxiedPlayer pl : main.getProxy().getPlayers()) {
                if (main.getPermissionHandler().hasPermission(pl, permission, true)) {
                    replace += format.replace("%player%", pl.getName()) //
                            .replace("%prefix%", main.getPlayerHandler().getPrefix(pl)) //
                            .replace("%suffix%", main.getPlayerHandler().getSuffix(pl)) //
                            .replace("%server%", pl.getServer().getInfo().getName()) + ChatColor.RESET + ", ";
                }
            }
            if (replace.length() > 1)
                replace = replace.substring(0, replace.length() - 2);
            output = output.replace("%" + full + "%", replace);
        }

        return output.replace("%player%", p.getName()) //
                .replace("%prefix%", main.getPlayerHandler().getPrefix(p)) //
                .replace("%suffix%", main.getPlayerHandler().getSuffix(p)) //
                .replace("%server%", p.getServer().getInfo().getName()) //
                .replace("%playersOnlineBungee%", "" + main.getProxy().getPlayers().size()) //
                .replace("%playersOnlineServer%", "" + p.getServer().getInfo().getPlayers().size());
    }
}
