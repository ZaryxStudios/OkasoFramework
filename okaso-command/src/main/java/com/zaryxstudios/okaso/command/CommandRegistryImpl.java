package com.zaryxstudios.okaso.command;

import com.zaryxstudios.okaso.common.OkasoAPI;
import com.zaryxstudios.okaso.common.command.CommandContext;
import com.zaryxstudios.okaso.common.command.CommandHandler;
import com.zaryxstudios.okaso.common.command.CommandSender;
import com.zaryxstudios.okaso.common.command.TabCompleter;
import com.zaryxstudios.okaso.common.command.annotation.Command;
import com.zaryxstudios.okaso.common.command.annotation.SubCommand;
import com.zaryxstudios.okaso.common.message.DefaultMessageProvider;
import com.zaryxstudios.okaso.common.message.MessageProvider;
import com.zaryxstudios.okaso.common.message.Messages;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Getter;

public class CommandRegistryImpl {

    private final Map<String, CommandEntry> commands;
    private final Map<UUID, Map<String, Long>> cooldowns;
    private MessageProvider messageProvider;

    public CommandRegistryImpl() {
        this.commands = new LinkedHashMap<>();
        this.cooldowns = new HashMap<>();
    }
    public void setMessageProvider(MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

    private MessageProvider provider() {
        if (messageProvider != null) return messageProvider;
        try {
            MessageProvider svc = OkasoAPI.service(MessageProvider.class);
            if (svc != null) {
                messageProvider = svc;
                return svc;
            }
        } catch (Exception ignored) {
        }
        return new DefaultMessageProvider();
    }

    public void registerCommand(Object handler) {
        Class<?> clazz = handler.getClass();

        Command classCmd = clazz.getAnnotation(Command.class);
        if (classCmd != null) {
            registerCommandFromClass(handler, clazz, classCmd);
            return;
        }

        for (Method method : findAllMethods(clazz)) {
            Command cmdAnn = method.getAnnotation(Command.class);
            if (cmdAnn != null) {
                registerCommandMethod(handler, method, cmdAnn);
            }
        }
    }

    private void registerCommandFromClass(Object handler, Class<?> clazz, Command cmdAnn) {
        CommandEntry entry = new CommandEntry();
        entry.name = cmdAnn.name().toLowerCase();
        entry.permission = cmdAnn.permission();
        entry.description = cmdAnn.description();
        entry.usage = cmdAnn.usage();
        entry.playerOnly = cmdAnn.playerOnly();
        entry.consoleOnly = cmdAnn.consoleOnly();
        entry.cooldown = cmdAnn.cooldown();
        entry.instance = handler;

        if (handler instanceof CommandHandler) {
            entry.handler = (CommandHandler) handler;
        }

        for (Method method : findAllMethods(clazz)) {
            SubCommand subAnn = method.getAnnotation(SubCommand.class);
            if (subAnn != null) {
                registerSubCommandFromMethod(entry, handler, method, subAnn);
                continue;
            }
            Command mCmdAnn = method.getAnnotation(Command.class);
            if (mCmdAnn != null) {
                registerCommandMethod(handler, method, mCmdAnn);
            }
        }

        commands.put(entry.name, entry);
        for (String alias : cmdAnn.aliases()) {
            commands.put(alias.toLowerCase(), entry);
        }
    }

    private void registerSubCommandFromMethod(CommandEntry parent, Object handler, Method method, SubCommand ann) {
        SubCommandEntry sub = new SubCommandEntry();
        sub.name = ann.name().toLowerCase();
        sub.aliases = Arrays.stream(ann.aliases()).map(String::toLowerCase).toArray(String[]::new);
        sub.permission = ann.permission();
        sub.description = ann.description();
        sub.playerOnly = ann.playerOnly();
        sub.method = method;
        sub.instance = handler;

        parent.subCommands.put(sub.name, sub);
        for (String alias : sub.aliases) {
            parent.subCommands.put(alias, sub);
        }
    }

    public void registerCommand(String name, CommandHandler handler) {
        registerCommand(name, null, "", "", "", handler);
    }

    public void registerCommand(String name, CommandHandler handler, TabCompleter tabCompleter) {
        registerCommand(name, null, "", "", "", handler);
        CommandEntry entry = commands.get(name.toLowerCase());
        if (entry != null) {
            entry.tabCompleter = tabCompleter;
        }
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

        return dispatchToEntry(sender, label, args, entry);
    }

    private boolean dispatchToEntry(CommandSender sender, String label, String[] args, CommandEntry entry) {
        if (entry.permission != null && !entry.permission.isEmpty()) {
            if (!sender.hasPermission(entry.permission)) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }
        }

        if (entry.playerOnly && !sender.isPlayer()) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (entry.consoleOnly && sender.isPlayer()) {
            sender.sendMessage("§cOnly the console can use this command.");
            return true;
        }

        if (entry.cooldown > 0 && sender.isPlayer()) {
            String name = sender.getName();
            UUID uuid = UUID.nameUUIDFromBytes(name.getBytes());
            long now = System.currentTimeMillis();
            Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(uuid, k -> new HashMap<>());
            Long lastUse = playerCooldowns.get(entry.name);
            if (lastUse != null && (now - lastUse) < entry.cooldown * 50L) {
                long remaining = (entry.cooldown * 50L - (now - lastUse)) / 50;
                sender.sendMessage("§cPlease wait " + remaining + " ticks before using this command again.");
                return true;
            }
            playerCooldowns.put(entry.name, now);
        }
        if (args != null && args.length > 0 && !entry.subCommands.isEmpty()) {
            String subName = args[0].toLowerCase();
            SubCommandEntry sub = entry.subCommands.get(subName);
            if (sub != null) {
                return dispatchSubCommand(sender, args, entry, sub);
            }
        }

        if (args != null && args.length > 0 && !entry.subCommands.isEmpty()) {
            showHelp(sender, entry);
            return true;
        }

        List<String> cmdArgs = args != null
            ? Arrays.asList(args)
            : Collections.emptyList();
        CommandContext ctx = new CommandContext(sender, entry.name, cmdArgs);
        invokeHandler(entry, ctx);
        return true;
    }

    private boolean dispatchSubCommand(CommandSender sender, String[] args, CommandEntry parent, SubCommandEntry sub) {
        if (sub.permission != null && !sub.permission.isEmpty()) {
            if (!sender.hasPermission(sub.permission)) {
                sender.sendMessage(provider().get(Messages.COMMAND_SUB_NO_PERMISSION));
                return true;
            }
        }

        if (sub.playerOnly && !sender.isPlayer()) {
            sender.sendMessage(provider().get(Messages.COMMAND_SUB_PLAYER_ONLY));
            return true;
        }

        List<String> subArgs = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            subArgs.add(args[i]);
        }
        CommandContext ctx = new CommandContext(sender, sub.name, subArgs);
        invokeSubHandler(sub, ctx);
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

        if (args != null) {
            if (args.length == 1) {
                if (!entry.subCommands.isEmpty()) {
                    List<String> suggestions = new ArrayList<>();
                    String prefix = args[0].toLowerCase();
                    for (SubCommandEntry sub : entry.subCommands.values()) {
                        if (sub.permission == null || sub.permission.isEmpty() || sender.hasPermission(sub.permission)) {
                            if (sub.name.startsWith(prefix) && !suggestions.contains(sub.name)) {
                                suggestions.add(sub.name);
                            }
                        }
                    }
                    return suggestions;
                }
            } else if (args.length > 1 && !entry.subCommands.isEmpty()) {
                String subName = args[0].toLowerCase();
                SubCommandEntry sub = entry.subCommands.get(subName);
                if (sub != null && sub.tabCompleter != null) {
                    List<String> subArgs = new ArrayList<>(Arrays.asList(args));
                    subArgs.remove(0);
                    CommandContext ctx = new CommandContext(sender, sub.name, subArgs);
                    return sub.tabCompleter.onTabComplete(ctx);
                }
            }
        }
        if (entry.tabCompleter != null) {
            List<String> cmdArgs = args != null
                ? Arrays.asList(args)
                : Collections.emptyList();
            CommandContext ctx = new CommandContext(sender, entry.name, cmdArgs);
            return entry.tabCompleter.onTabComplete(ctx);
        }

        return Collections.emptyList();
    }


    public void unregisterCommand(String name) {
        String key = name.toLowerCase();
        CommandEntry entry = commands.get(key);
        if (entry == null) return;

        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, CommandEntry> e : commands.entrySet()) {
            if (e.getValue() == entry) {
                toRemove.add(e.getKey());
            }
        }
        for (String k : toRemove) {
            commands.remove(k);
        }
        if (entry.subCommands != null) {
            entry.subCommands.clear();
        }
    }

    public void clear() {
        commands.clear();
        cooldowns.clear();
    }

    public Optional<CommandEntry> getCommand(String name) {
        return Optional.ofNullable(commands.get(name.toLowerCase()));
    }

    public Map<String, CommandEntry> getCommands() {
        return Collections.unmodifiableMap(commands);
    }

    public boolean isRegistered(String name) {
        return commands.containsKey(name.toLowerCase());
    }

    private void registerCommandMethod(Object handler, Method method, Command cmdAnn) {
        CommandEntry entry = new CommandEntry();
        entry.name = cmdAnn.name().toLowerCase();
        entry.permission = cmdAnn.permission();
        entry.description = cmdAnn.description();
        entry.usage = cmdAnn.usage();
        entry.playerOnly = cmdAnn.playerOnly();
        entry.consoleOnly = cmdAnn.consoleOnly();
        entry.cooldown = cmdAnn.cooldown();
        entry.instance = handler;
        entry.method = method;

        ScanResult scan = scanSubCommands(handler);
        for (SubCommandEntry sub : scan.subCommands.values()) {
            entry.subCommands.put(sub.name, sub);
            if (sub.aliases != null) {
                for (String alias : sub.aliases) {
                    entry.subCommands.put(alias, sub);
                }
            }
        }

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
                if (sub.aliases != null) {
                    for (String alias : sub.aliases) {
                        parent.subCommands.put(alias, sub);
                    }
                }
            }
        }
    }

    private ScanResult scanSubCommands(Object handler) {
        ScanResult result = new ScanResult();
        Class<?> clazz = handler.getClass();
        for (Method method : findAllMethods(clazz)) {
            SubCommand subAnn = method.getAnnotation(SubCommand.class);
            if (subAnn == null) continue;

            SubCommandEntry sub = new SubCommandEntry();
            sub.name = subAnn.name().toLowerCase();
            sub.aliases = Arrays.stream(subAnn.aliases()).map(String::toLowerCase).toArray(String[]::new);
            sub.permission = subAnn.permission();
            sub.description = subAnn.description();
            sub.playerOnly = subAnn.playerOnly();
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

    public void showHelp(CommandSender sender, CommandEntry entry) {
        MessageProvider p = provider();
        sender.sendMessage(p.format(Messages.COMMAND_HELP_HEADER, entry.name, entry.description));
        if (!entry.subCommands.isEmpty()) {
            sender.sendMessage(p.get(Messages.COMMAND_HELP_SUBTITLE));
            for (SubCommandEntry sub : entry.subCommands.values()) {
                if (sub.permission == null || sub.permission.isEmpty() || sender.hasPermission(sub.permission)) {
                    sender.sendMessage(p.format(Messages.COMMAND_HELP_ENTRY, sub.name, sub.description));
                }
            }
        }
    }

    public void showHelp(CommandSender sender, String commandName) {
        getCommand(commandName).ifPresent(entry -> showHelp(sender, entry));
    }

    public void clearCooldowns() {
        cooldowns.clear();
    }

    public void clearCooldowns(UUID playerId) {
        cooldowns.remove(playerId);
    }

    private CommandHandler createReflectiveHandler(final Object instance, final Method method) {
        return new CommandHandler() {
            @Override
            public void execute(CommandContext context) {
                try {
                    method.setAccessible(true);
                    Class<?>[] paramTypes = method.getParameterTypes();
                    if (paramTypes.length == 1 && CommandContext.class.isAssignableFrom(paramTypes[0])) {
                        method.invoke(instance, context);
                    } else {
                        method.invoke(instance);
                    }
                } catch (Exception e) {
                    context.getSender().sendMessage(provider().get(Messages.COMMAND_ERROR));
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

    private void invokeSubHandler(SubCommandEntry entry, CommandContext ctx) {
        if (entry.method != null && entry.instance != null) {
            createReflectiveHandler(entry.instance, entry.method).execute(ctx);
        }
    }

    public static class CommandEntry {
        @Getter String name;
        @Getter String permission;
        @Getter String description;
        @Getter String usage;
        boolean playerOnly;
        boolean consoleOnly;
        int cooldown;
        CommandHandler handler;
        TabCompleter tabCompleter;
        @Getter Map<String, SubCommandEntry> subCommands = new LinkedHashMap<>();
        Object instance;
        Method method;

        public void setTabCompleter(TabCompleter tabCompleter) {
            this.tabCompleter = tabCompleter;
        }

        public boolean hasSubCommands() {
            return !subCommands.isEmpty();
        }

        public List<String> getSubCommandNames() {
            return new ArrayList<>(subCommands.keySet());
        }
    }

    public static class SubCommandEntry {
        @Getter String name;
        String[] aliases;
        @Getter String permission;
        @Getter String description;
        boolean playerOnly;
        String parentCommand;
        Method method;
        Object instance;
        TabCompleter tabCompleter;

        public void setTabCompleter(TabCompleter tabCompleter) {
            this.tabCompleter = tabCompleter;
        }
    }

    private static class ScanResult {
        Map<String, SubCommandEntry> subCommands = new LinkedHashMap<>();
        Map<String, SubCommandEntry> orphanSubCommands = new LinkedHashMap<>();
    }

    private static List<Method> findAllMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Method m : current.getDeclaredMethods()) {
                methods.add(m);
            }
            current = current.getSuperclass();
        }
        return methods;
    }
}
