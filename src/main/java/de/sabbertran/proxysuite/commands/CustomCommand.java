package de.sabbertran.proxysuite.commands;

import de.sabbertran.proxysuite.ProxySuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CustomCommand extends Command {
    private ProxySuite main;
    private String command;
    private String permission;
    private String[] messages;
    private ArrayList<String> disabledServers;

    public CustomCommand(ProxySuite main, String command, String permission, String[] messages, ArrayList<String>
            disabledServers) {
        super(command.trim());
        this.command = command.trim();
        this.main = main;
        this.permission = permission;
        this.messages = messages;
        this.disabledServers = disabledServers;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        main.getCommandHandler().executeCommand(sender, command, new Runnable() {
            public void run() {
                if (sender instanceof ProxiedPlayer) {
                    final ProxiedPlayer p = (ProxiedPlayer) sender;
                    if (permission.trim().equals("") || main.getPermissionHandler().hasPermission(sender, permission)) {
                        if (!disabledServers.contains(p.getServer().getInfo().getName())) {
                            for (final String s : messages) {
                                final int delay = s.matches("^%delay:[0-9]+%.*$") ? Integer.parseInt(s.split("%")[1].substring(6)) : 0;
                                Runnable runnable = new Runnable() {
                                    public void run() {
                                        String s2 = s.replace("%delay:" + delay + "%", "");
                                        if (s2.trim().startsWith("/") || s2.trim().startsWith("\\")) {
                                            s2 = s2.trim();
                                            for (int i = 0; i < args.length; i++)
                                                s2 = s2.replace("$" + (i + 1), args[i]);
                                            if (s2.startsWith("/"))
                                                p.chat(s2);
                                            else if (s2.startsWith("\\\\"))
                                                main.getProxy().getPluginManager().dispatchCommand(main.getProxy().getConsole(), s2.substring(2).replace("%player%", sender.getName()));
                                            else
                                                main.getProxy().getPluginManager().dispatchCommand(sender, s2.substring(1));
                                        } else {
                                            main.getMessageHandler().sendMessage(p, main.getCustomCommandHandler().translateVariables(s2, p));
                                        }
                                    }
                                };
                                if (delay > 0)
                                    main.getProxy().getScheduler().schedule(main, runnable, delay, TimeUnit.MILLISECONDS);
                                else
                                    runnable.run();
                            }
                        } else {
                            String cmd = "/" + command;
                            for (String s : args)
                                cmd += " " + args;
                            cmd = cmd.trim();
                            p.chat(cmd);
                        }
                    } else {
                        main.getPermissionHandler().sendMissingPermissionInfo(sender);
                    }
                } else
                    main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.noplayer"));
            }
        });
    }
}
