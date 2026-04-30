package me.m0dii.extraenchants.framework.registry;

import me.m0dii.extraenchants.framework.runtime.ExecutionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PlaceholderRegistry {
    private final Map<String, Function<ExecutionContext, String>> placeholders = new HashMap<>();

    public void register(String key, Function<ExecutionContext, String> provider) {
        placeholders.put(key.toLowerCase(), provider);
    }

    public String resolveAll(String text, ExecutionContext context) {
        if (text == null) {
            return "";
        }

        String result = text;

        for (Map.Entry<String, Function<ExecutionContext, String>> entry : placeholders.entrySet()) {
            String token = "%" + entry.getKey() + "%";
            result = result.replace(token, entry.getValue().apply(context));
        }

        return result;
    }
}

