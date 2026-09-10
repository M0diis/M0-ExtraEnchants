package me.m0dii.extraenchants.framework.registry;

import me.m0dii.extraenchants.framework.api.Condition;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class ConditionRegistry {
    private final Map<String, Function<Object, Condition>> conditions = new HashMap<>();

    public void register(String key, Function<Object, Condition> builder) {
        conditions.put(key.toLowerCase(Locale.ROOT), builder);
    }

    public Condition build(String key, Object rawValue) {
        Function<Object, Condition> factory = conditions.get(key.trim().toLowerCase(Locale.ROOT));
        return factory == null ? null : factory.apply(rawValue);
    }
}

