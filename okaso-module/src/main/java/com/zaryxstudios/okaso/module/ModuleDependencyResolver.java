package com.zaryxstudios.okaso.module;

import java.util.*;

public class ModuleDependencyResolver {

    private final Map<String, List<String>> dependencies;

    public ModuleDependencyResolver() {
        this.dependencies = new LinkedHashMap<>();
    }

    public void register(String moduleName, List<String> deps) {
        dependencies.put(moduleName, deps == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(deps)));
    }

    public void unregister(String moduleName) {
        dependencies.remove(moduleName);
    }

    public List<String> getEnableOrder() {
        List<String> order = new ArrayList<>();
        Set<String> permanent = new HashSet<>();
        Set<String> temporary = new HashSet<>();

        for (String module : dependencies.keySet()) {
            if (!permanent.contains(module)) {
                visit(module, order, permanent, temporary);
            }
        }
        return order;
    }

    public List<String> getDisableOrder() {
        List<String> order = getEnableOrder();
        Collections.reverse(order);
        return order;
    }

    public Map<String, List<String>> getDependencies() {
        return Collections.unmodifiableMap(dependencies);
    }

    private void visit(String module, List<String> order, Set<String> permanent, Set<String> temporary) {
        if (permanent.contains(module)) return;
        if (temporary.contains(module)) {
            throw new CycleDependencyException("Cycle detected involving module: " + module);
        }

        temporary.add(module);

        List<String> deps = dependencies.getOrDefault(module, Collections.emptyList());
        for (String dep : deps) {
            if (dependencies.containsKey(dep)) {
                visit(dep, order, permanent, temporary);
            }
        }

        temporary.remove(module);
        permanent.add(module);
        order.add(module);
    }

    public static class CycleDependencyException extends RuntimeException {
        public CycleDependencyException(String message) {
            super(message);
        }
    }
}
