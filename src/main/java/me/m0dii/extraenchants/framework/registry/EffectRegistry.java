package me.m0dii.extraenchants.framework.registry;

import me.m0dii.extraenchants.framework.api.EffectExecutor;

import java.util.HashMap;
import java.util.Map;

public class EffectRegistry {
    private final Map<String, EffectExecutor> effects = new HashMap<>();

    public void register(String key, EffectExecutor executor) {
        effects.put(key.toLowerCase(), executor);
    }

    public EffectExecutor get(String key) {
        return key == null ? null : effects.get(key.toLowerCase());
    }
}

