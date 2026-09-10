package me.m0dii.extraenchants.framework.registry;

import me.m0dii.extraenchants.framework.api.EffectExecutor;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EffectRegistry {
    private final Map<String, EffectExecutor> effects = new HashMap<>();

    public void register(String key, EffectExecutor executor) {
        effects.put(normalize(key), executor);
    }

    public EffectExecutor get(String key) {
        return key == null ? null : effects.get(normalize(key));
    }

    private String normalize(String key) {
        return key.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}

