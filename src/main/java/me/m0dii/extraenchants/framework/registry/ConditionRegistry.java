package me.m0dii.extraenchants.framework.registry;

import me.m0dii.extraenchants.framework.api.Condition;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ConditionRegistry {
    private final Map<String, Function<Object, Condition>> conditions = new HashMap<>();

    public void register(String key, Function<Object, Condition> builder) {
        conditions.put(key.toLowerCase(), builder);
    }

    public Condition build(String key, Object rawValue) {
        Function<Object, Condition> factory = conditions.get(key.toLowerCase());
        return factory == null ? null : factory.apply(rawValue);
    }
}

