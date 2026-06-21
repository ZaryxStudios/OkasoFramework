package com.zaryxstudios.okaso.command;

import com.zaryxstudios.okaso.common.command.CommandContext;
import com.zaryxstudios.okaso.common.command.CommandHandler;
import com.zaryxstudios.okaso.common.command.CommandSender;
import com.zaryxstudios.okaso.common.command.TabCompleter;
import com.zaryxstudios.okaso.common.command.annotation.Command;
import com.zaryxstudios.okaso.common.command.annotation.SubCommand;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistryImpl {

    private final Map<String, CommandEntry> commands;

    public CommandRegistryImpl() {
        this.commands = new LinkedHashMap<String, CommandEntry>();
    }

    public void registerCommand(Object handler) {
        Class<?> clazz = handler.getClass();
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            Command cmdAnn = method.getAnnotation(Command.class);
            if (cmdAnn != null) {
                registerCommandMethod(handler, method, cmdAnn);
            }
        }
    }

    public void registerCommand(String name, CommandHandler handler) {
        registerCommand(name, null, "", "", "", handler);
    }

    public void registerCommand(String name, String[] aliases, String permission,
                                String description, String usage, CommandHandler handler) {
        CommandEntry entry = new CommandEntry();
        entry.name = name.toLowerCase();
        entry.permission = permission;
        entry.description = description;
        entry.usage = usage;
        entry.handler = handler;
        entry.instance = null;
        entry.method = null;

        commands.put(entry.name, entry);
        if (aliases != null) {
            for (String alias : aliases) {
                commands.put(alias.toLowerCase(), entry);
            }
        }
    }

    public boolean dispatch(CommandSender sender, String label, String[] args) {
        if (label == null) return false;

        String cmdName = label.toLowerCase();
        CommandEntry entry = commands.get(cmdName);
        if (entry == null) return false;

        if (entry.permission != null && !entry.permission.isEmpty()) {
            if (!sender.hasPermission(entry.permission)) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }
        }

        if (args != null && args.length > 0 && !entry.subCommands.isEmpty()) {
            String subName = args[0].toLowerCase();
            SubCommandEntry sub = entry.subCommands.get(subName);
            if (sub != null) {
                if (sub.permission != null && !sub.permission.isEmpty()) {
                    if (!sender.hasPermission(sub.permission)) {
                        sender.sendMessage("§cYou don't have permission to use this sub-command.");
                        return true;
                    }
                }
                List<String> subArgs = new ArrayList<String>();
                for (int i = 1; i < args.length; i++) {
                    subArgs.add(args[i]);
                }
                CommandContext ctx = new CommandContext(sender, subName, subArgs);
                invokeHandler(sub, ctx);
                return true;
            }
        }

        List<String> cmdArgs = args != null
            ? Arrays.asList(args)
            : Collections.<String>emptyList();
        CommandContext ctx = new CommandContext(sender, entry.name, cmdArgs);
        invokeHandler(entry, ctx);
        return true;
    }

    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        if (label == null) return Collections.emptyList();

        String cmdName = label.toLowerCase();
        CommandEntry entry = commands.get(cmdName);
        if (entry == null) return Collections.emptyList();

        if (entry.permission != null && !entry.permission.isEmpty()) {
            if (!sender.hasPermission(entry.permission)) {
                return Collections.emptyList();
            }
        }

        if (args != null && args.length == 1 && !entry.subCommands.isEmpty()) {
            List<String> suggestions = new ArrayList<String>();
            String prefix = args[0].toLowerCase();
            for (SubCommandEntry sub : entry.subCommands.values()) {
                if (sub.permission == null || sub.permission.isEmpty() || sender.hasPermission(sub.permission)) {
                    if (sub.name.startsWith(prefix)) {
                        suggestions.add(sub.name);
                    }
                }
            }
            return suggestions;
        }

        if (entry.tabCompleter != null) {
            List<String> cmdArgs = args != null
                ? Arrays.asList(args)
                : Collections.<String>emptyList();
            CommandContext ctx = new CommandContext(sender, entry.name, cmdArgs);
            return entry.tabCompleter.onTabComplete(ctx);
        }

        return Collections.emptyList();
    }

    public void registerCommand(String name, CommandHandler handler, TabCompleter tabCompleter) {
        registerCommand(name, null, "", "", "", handler);
        CommandEntry entry = commands.get(name.toLowerCase());
        if (entry != null) {
            entry.tabCompleter = tabCompleter;
        }
    }

    public void unregisterCommand(String name) {
        CommandEntry entry = commands.remove(name.toLowerCase());
        if (entry != null && entry.name.equals(name.toLowerCase())) {
            if (entry.subCommands != null) {
                entry.subCommands.clear();
            }
        }
    }

    public void clear() {
        commands.clear();
    }

    public Map<String, CommandEntry> getCommands() {
        return Collections.unmodifiableMap(commands);
    }

    private void registerCommandMethod(Object handler, Method method, Command cmdAnn) {
        CommandEntry entry = new CommandEntry();
        entry.name = cmdAnn.name().toLowerCase();
        entry.permission = cmdAnn.permission();
        entry.description = cmdAnn.description();
        entry.usage = cmdAnn.usage();
        entry.instance = handler;
        entry.method = method;

        ScanResult scan = scanSubCommands(handler);
        entry.subCommands.putAll(scan.subCommands);

        if (CommandHandler.class.isAssignableFrom(handler.getClass())) {
            entry.handler = (CommandHandler) handler;
        } else {
            entry.handler = createReflectiveHandler(handler, method);
        }

        commands.put(entry.name, entry);

        for (String alias : cmdAnn.aliases()) {
            commands.put(alias.toLowerCase(), entry);
        }

        for (Map.Entry<String, SubCommandEntry> subEntry : scan.orphanSubCommands.entrySet()) {
            SubCommandEntry sub = subEntry.getValue();
            CommandEntry parent = commands.get(sub.parentCommand);
            if (parent != null) {
                parent.subCommands.put(sub.name, sub);
            }
        }
    }

    private ScanResult scanSubCommands(Object handler) {
        ScanResult result = new ScanResult();
        Class<?> clazz = handler.getClass();
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            SubCommand subAnn = method.getAnnotation(SubCommand.class);
            if (subAnn == null) continue;

            SubCommandEntry sub = new SubCommandEntry();
            sub.name = subAnn.name().toLowerCase();
            sub.permission = subAnn.permission();
            sub.description = subAnn.description();
            sub.method = method;
            sub.instance = handler;

            Command parentCmd = method.getDeclaringClass().getAnnotation(Command.class);
            if (parentCmd != null) {
                sub.parentCommand = parentCmd.name().toLowerCase();
                result.subCommands.put(sub.name, sub);
            } else {
                result.orphanSubCommands.put(sub.name, sub);
            }
        }

        return result;
    }

    private CommandHandler createReflectiveHandler(final Object instance, final Method method) {
        return new CommandHandler() {
            @Override
            public void execute(CommandContext context) {
                try {
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    Class<?>[] paramTypes = method.getParameterTypes();
                    if (paramTypes.length == 1 && CommandContext.class.isAssignableFrom(paramTypes[0])) {
                        method.invoke(instance, context);
                    } else {
                        method.invoke(instance);
                    }
                } catch (Exception e) {
                    context.getSender().sendMessage("§cAn error occurred while executing this command.");
                    e.printStackTrace();
                }
            }
        };
    }

    private void invokeHandler(CommandEntry entry, CommandContext ctx) {
        if (entry.handler != null) {
            entry.handler.execute(ctx);
        } else if (entry.method != null && entry.instance != null) {
            createReflectiveHandler(entry.instance, entry.method).execute(ctx);
        }
    }

    private void invokeHandler(SubCommandEntry entry, CommandContext ctx) {
        if (entry.method != null && entry.instance != null) {
            createReflectiveHandler(entry.instance, entry.method).execute(ctx);
        }
    }

    public static class CommandEntry {
        String name;
        String permission;
        String description;
        String usage;
        CommandHandler handler;
        TabCompleter tabCompleter;
        Map<String, SubCommandEntry> subCommands = new LinkedHashMap<String, SubCommandEntry>();
        Object instance;
        Method method;

        public String getName() { return name; }
        public String getPermission() { return permission; }
        public String getDescription() { return description; }
        public String getUsage() { return usage; }
        public Map<String, SubCommandEntry> getSubCommands() { return subCommands; }

        public void setTabCompleter(TabCompleter tabCompleter) {
            this.tabCompleter = tabCompleter;
        }
    }

    public static class SubCommandEntry {
        String name;
        String permission;
        String description;
        String parentCommand;
        Method method;
        Object instance;

        public String getName() { return name; }
        public String getPermission() { return permission; }
        public String getDescription() { return description; }
    }

    private static class ScanResult {
        Map<String, SubCommandEntry> subCommands = new LinkedHashMap<String, SubCommandEntry>();
        Map<String, SubCommandEntry> orphanSubCommands = new LinkedHashMap<String, SubCommandEntry>();
    }
}
