package com.zaryxstudios.okaso.common.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.Getter;

@Getter
public class CommandContext {

    private final String label;
    private final List<String> args;
    private final CommandSender sender;

    public CommandContext(CommandSender sender, String label, List<String> args) {
        this.sender = sender;
        this.label = label;
        this.args = args != null ? Collections.unmodifiableList(new ArrayList<>(args)) : Collections.emptyList();
    }

    public int length() { return args.size(); }

    public boolean hasArg(int index) { return index >= 0 && index < args.size(); }

    public Optional<String> arg(int index) {
        return hasArg(index) ? Optional.of(args.get(index)) : Optional.empty();
    }

    public String joinArgs(int start) {
        if (start >= args.size()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.size(); i++) {
            if (i > start) sb.append(' ');
            sb.append(args.get(i));
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getSenderAs(Class<T> type) {
        if (type.isInstance(sender)) return Optional.of((T) sender);
        return Optional.empty();
    }
}
