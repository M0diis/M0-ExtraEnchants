package me.m0dii.extraenchants.framework.config;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.framework.model.CustomEnchantDefinition;
import me.m0dii.extraenchants.framework.model.EffectDefinition;
import me.m0dii.extraenchants.framework.model.TriggerDefinition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CustomEnchantConfigLoader {
    private final ExtraEnchants plugin;
    private final ConditionParser conditionParser;

    public CustomEnchantConfigLoader(ExtraEnchants plugin, ConditionParser conditionParser) {
        this.plugin = plugin;
        this.conditionParser = conditionParser;
    }

    public Map<String, CustomEnchantDefinition> load(File directory) {
        Map<String, CustomEnchantDefinition> loaded = new HashMap<>();
        Set<String> wrapperIds = Arrays.stream(EEnchant.values())
                .map(EEnchant::getConfigName)
                .collect(Collectors.toSet());

        if (!directory.exists() && !directory.mkdirs()) {
            return loaded;
        }

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return loaded;
        }

        for (File file : files) {
            try {
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
                String id = yml.getString("id", file.getName().replace(".yml", ""));
                CustomEnchantDefinition definition = new CustomEnchantDefinition(id.toLowerCase());

                if (wrapperIds.contains(definition.getId())) {
                    plugin.getLogger().warning("Skipping custom enchant '" + definition.getId() + "' from " + file.getName()
                            + " because that id is reserved by a wrapper enchant");
                    continue;
                }

                if (loaded.containsKey(definition.getId())) {
                    plugin.getLogger().warning("Skipping duplicate custom enchant id '" + definition.getId() + "' from "
                            + file.getName() + " (already loaded from another file)");
                    continue;
                }

                definition.setDisplayName(yml.getString("display-name", id));
                definition.setDescription(yml.getString("description", ""));
                definition.setRarity(yml.getString("rarity", "COMMON"));
                definition.setMaxLevel(Math.max(1, yml.getInt("max-level", 1)));
                definition.setEnabled(yml.getBoolean("enabled", true));
                definition.setShowInList(yml.getBoolean("show-in-list", true));
                definition.setWeight(yml.getInt("weight", yml.getInt("spawn-chance", 0)));
                definition.setIcon(yml.getString("icon", "ENCHANTED_BOOK"));
                definition.setCategory(yml.getString("category", "GENERAL"));
                definition.getApplicableItems().addAll(yml.getStringList("applicable-items"));
                definition.getConflictsWith().addAll(yml.getStringList("conflicts-with"));

                ConfigurationSection triggers = yml.getConfigurationSection("triggers");
                if (triggers != null) {
                    for (String triggerName : triggers.getKeys(false)) {
                        ConfigurationSection triggerSection = triggers.getConfigurationSection(triggerName);
                        if (triggerSection == null) {
                            continue;
                        }

                        TriggerDefinition trigger = new TriggerDefinition(triggerName);
                        trigger.setChance(triggerSection.getString("chance", "100"));
                        trigger.setCooldown(triggerSection.getString("cooldown", "0"));
                        trigger.setGlobalCooldown(triggerSection.getString("global-cooldown", "0"));
                        trigger.setTarget(triggerSection.getString("target", "VICTIM"));
                        trigger.setRadius(triggerSection.getString("radius", "6"));
                        trigger.setPriority(triggerSection.getInt("priority", 0));
                        trigger.setDelayTicks(triggerSection.getInt("delay", 0));
                        trigger.setRepeatCount(triggerSection.getInt("repeat", 1));
                        trigger.setRepeatIntervalTicks(triggerSection.getInt("repeat-interval", 1));
                        trigger.setCancelEvent(triggerSection.getBoolean("cancel-event", false));
                        trigger.setActivationLimit(triggerSection.getInt("activation-limit", -1));
                        trigger.getChainTriggers().addAll(triggerSection.getStringList("chain-triggers"));

                        Object conditionsRaw = triggerSection.get("conditions");
                        if (conditionsRaw != null) {
                            trigger.getConditions().add(conditionParser.parse(conditionsRaw));
                        }

                        List<Map<?, ?>> effectRows = triggerSection.getMapList("effects");
                        for (Map<?, ?> row : effectRows) {
                            for (Map.Entry<?, ?> entry : row.entrySet()) {
                                trigger.getEffects().add(new EffectDefinition(String.valueOf(entry.getKey()), entry.getValue()));
                            }
                        }

                        definition.getTriggers().put(triggerName, trigger);
                    }
                }

                loaded.put(definition.getId(), definition);
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load custom enchant file " + file.getName(), ex);
            }
        }

        return loaded;
    }
}

