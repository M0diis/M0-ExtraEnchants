package me.m0dii.extraenchants.framework.registry;

import me.m0dii.extraenchants.framework.api.TargetSelector;

import java.util.HashMap;
import java.util.Map;

public class TargetRegistry {
    private final Map<String, TargetSelector> selectors = new HashMap<>();

    public void register(String key, TargetSelector selector) {
        selectors.put(key.toUpperCase(), selector);
    }

    public TargetSelector get(String key) {
        if (key == null || key.isBlank()) {
            return selectors.get("VICTIM");
        }

        return selectors.getOrDefault(key.toUpperCase(), selectors.get("VICTIM"));
    }
}

