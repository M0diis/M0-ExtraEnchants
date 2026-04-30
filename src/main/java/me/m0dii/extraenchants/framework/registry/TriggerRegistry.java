package me.m0dii.extraenchants.framework.registry;

import me.m0dii.extraenchants.framework.model.CustomEnchantDefinition;
import me.m0dii.extraenchants.framework.runtime.TriggerType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TriggerRegistry {
    private final Map<TriggerType, List<CustomEnchantDefinition>> byTrigger = new HashMap<>();

    public void rebuild(Iterable<CustomEnchantDefinition> definitions) {
        byTrigger.clear();

        for (CustomEnchantDefinition definition : definitions) {
            definition.getTriggers().forEach((triggerName, triggerDefinition) -> {
                TriggerType type = TriggerType.fromKey(triggerName);
                if (type == null) {
                    return;
                }

                byTrigger.computeIfAbsent(type, ignored -> new ArrayList<>()).add(definition);
            });
        }
    }

    public List<CustomEnchantDefinition> getByTrigger(TriggerType type) {
        return byTrigger.getOrDefault(type, List.of());
    }
}

