package me.m0dii.extraenchants.framework.config;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.framework.model.CustomEnchantDefinition;
import me.m0dii.extraenchants.framework.model.EffectDefinition;
import me.m0dii.extraenchants.framework.model.ItemApplicability;
import me.m0dii.extraenchants.framework.model.TriggerDefinition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

/**
 * Loads custom enchant definitions into an isolated result. Callers can keep
 * their current registry when the result is not committed, which makes reload
 * atomic from the runtime's point of view.
 */
public class CustomEnchantConfigLoader {
    private final ExtraEnchants plugin;
    private final ConditionParser conditionParser;
    private final CustomEnchantConfigSchema schema;
    private final CustomEnchantConfigValidator validator = new CustomEnchantConfigValidator();
    private volatile CustomEnchantLoadResult lastResult = new CustomEnchantLoadResult(
            Map.of(),
            List.of(),
            0,
            0,
            false
    );

    public CustomEnchantConfigLoader(ExtraEnchants plugin, ConditionParser conditionParser) {
        this(plugin, conditionParser, new CustomEnchantConfigSchema());
    }

    public CustomEnchantConfigLoader(
            ExtraEnchants plugin,
            ConditionParser conditionParser,
            CustomEnchantConfigSchema schema
    ) {
        this.plugin = plugin;
        this.conditionParser = conditionParser;
        this.schema = schema == null ? new CustomEnchantConfigSchema() : schema;
    }

    /**
     * Backwards-compatible convenience API. The detailed validation result is
     * available through {@link #loadResult(File)} and {@link #getLastResult()}.
     */
    public Map<String, CustomEnchantDefinition> load(File directory) {
        return loadResult(directory).getDefinitions();
    }

    public CustomEnchantLoadResult loadResult(File directory) {
        List<ConfigValidationIssue> issues = new ArrayList<>();
        Map<String, CustomEnchantDefinition> loaded = new LinkedHashMap<>();
        Set<String> wrapperIds = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Arrays.stream(EEnchant.values())
                .map(EEnchant::getConfigName)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .forEach(wrapperIds::add);

        if (directory == null) {
            issues.add(error("<unknown>", "directory", "Configuration directory must not be null"));
            return remember(new CustomEnchantLoadResult(loaded, issues, 0, 0, false));
        }

        if (!directory.exists() && !directory.mkdirs()) {
            issues.add(error(directory.getName(), "directory", "Unable to create configuration directory"));
            return remember(new CustomEnchantLoadResult(loaded, issues, 0, 0, false));
        }

        File[] files = directory.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null) {
            issues.add(error(directory.getName(), "directory", "Unable to list configuration files"));
            return remember(new CustomEnchantLoadResult(loaded, issues, 0, 0, false));
        }

        Arrays.sort(files, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(File::getName));
        int validFiles = 0;

        for (File file : files) {
            String fileName = file.getName();
            List<ConfigValidationIssue> fileIssues = new ArrayList<>();
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                int schemaVersion = schema.readVersion(yaml, fileIssues::add, fileName);
                if (schemaVersion <= CustomEnchantConfigSchema.CURRENT_VERSION) {
                    schema.migrate(yaml, schemaVersion, fileIssues::add, fileName);
                }

                fileIssues.addAll(validator.validate(yaml, fileName));
                String id = normalizedId(yaml.getString("id"), fileName);
                if (wrapperIds.contains(id)) {
                    fileIssues.add(error(fileName, "id", "Id is reserved by a built-in wrapper enchant"));
                }
                if (loaded.containsKey(id)) {
                    fileIssues.add(error(fileName, "id", "Duplicate custom enchant id '" + id + "'"));
                }

                issues.addAll(fileIssues);
                if (hasErrors(fileIssues)) {
                    continue;
                }

                CustomEnchantDefinition definition = parseDefinition(yaml, id);
                loaded.put(id, definition);
                validFiles++;
            } catch (Exception ex) {
                issues.add(error(fileName, "", "Failed to load configuration: " + safeMessage(ex)));
                if (plugin != null) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to load custom enchant file " + fileName, ex);
                }
            }
        }

        boolean committed = !hasErrors(issues) && validFiles == files.length;
        if (!committed) {
            loaded = new LinkedHashMap<>();
        }

        return remember(new CustomEnchantLoadResult(loaded, issues, files.length, validFiles, committed));
    }

    public CustomEnchantLoadResult getLastResult() {
        return lastResult;
    }

    private CustomEnchantLoadResult remember(CustomEnchantLoadResult result) {
        lastResult = result;
        return result;
    }

    private CustomEnchantDefinition parseDefinition(YamlConfiguration yaml, String id) {
        CustomEnchantDefinition definition = new CustomEnchantDefinition(id);
        definition.setDisplayName(yaml.getString("display-name", id));
        definition.setDescription(yaml.getString("description", ""));
        definition.setRarity(yaml.getString("rarity", "COMMON"));
        definition.setMaxLevel(Math.max(1, yaml.getInt("max-level", 1)));
        definition.setEnabled(yaml.getBoolean("enabled", true));
        definition.setShowInList(yaml.getBoolean("show-in-list", true));
        definition.setWeight(yaml.contains("weight")
                ? yaml.getInt("weight")
                : yaml.getInt("spawn-chance", 0));
        definition.setIcon(yaml.getString("icon", "ENCHANTED_BOOK"));
        definition.setCategory(yaml.getString("category", "GENERAL"));

        for (String rawItem : yaml.getStringList("applicable-items")) {
            ItemApplicability.parse(rawItem).ifPresent(item -> definition.getApplicableItems().add(item.name()));
        }
        for (String conflict : yaml.getStringList("conflicts-with")) {
            String normalized = conflict == null ? "" : conflict.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isBlank()) {
                definition.getConflictsWith().add(normalized);
            }
        }

        ConfigurationSection metadata = yaml.getConfigurationSection("metadata");
        if (metadata != null) {
            definition.getMetadata().putAll(metadata.getValues(false));
        }

        ConfigurationSection triggers = yaml.getConfigurationSection("triggers");
        if (triggers != null) {
            List<String> triggerNames = new ArrayList<>(triggers.getKeys(false));
            triggerNames.sort(String.CASE_INSENSITIVE_ORDER);
            for (String triggerName : triggerNames) {
                ConfigurationSection section = triggers.getConfigurationSection(triggerName);
                if (section == null) {
                    continue;
                }

                TriggerDefinition trigger = new TriggerDefinition(triggerName);
                trigger.setChance(section.getString("chance", "100"));
                trigger.setCooldown(section.getString("cooldown", "0"));
                trigger.setGlobalCooldown(section.getString("global-cooldown", "0"));
                trigger.setTarget(section.getString("target", "VICTIM").toUpperCase(Locale.ROOT));
                trigger.setRadius(section.getString("radius", "6"));
                trigger.setPriority(section.getInt("priority", 0));
                trigger.setDelayTicks(section.getInt("delay", 0));
                trigger.setRepeatCount(section.getInt("repeat", 1));
                trigger.setRepeatIntervalTicks(section.getInt("repeat-interval", 1));
                trigger.setCancelEvent(section.getBoolean("cancel-event", false));
                trigger.setActivationLimit(section.getInt("activation-limit", -1));

                for (String chain : section.getStringList("chain-triggers")) {
                    if (chain != null && !chain.isBlank()) {
                        trigger.getChainTriggers().add(chain.trim());
                    }
                }

                Object conditionsRaw = section.get("conditions");
                if (conditionsRaw != null) {
                    trigger.getConditions().add(conditionParser.parse(conditionsRaw));
                }

                List<Map<?, ?>> effectRows = section.getMapList("effects");
                for (Map<?, ?> row : effectRows) {
                    for (Map.Entry<?, ?> entry : row.entrySet()) {
                        trigger.getEffects().add(new EffectDefinition(
                                String.valueOf(entry.getKey())
                                        .trim()
                                        .toLowerCase(Locale.ROOT)
                                        .replace('-', '_'),
                                entry.getValue()
                        ));
                    }
                }

                definition.getTriggers().put(triggerName, trigger);
            }
        }

        return definition;
    }

    private String normalizedId(String rawId, String fileName) {
        String lowerFileName = fileName.toLowerCase(Locale.ROOT);
        String fallback = lowerFileName.endsWith(".yml")
                ? fileName.substring(0, fileName.length() - 4)
                : fileName;
        String value = rawId == null || rawId.isBlank() ? fallback : rawId;
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean hasErrors(List<ConfigValidationIssue> issues) {
        return issues.stream().anyMatch(ConfigValidationIssue::isError);
    }

    private ConfigValidationIssue error(String file, String path, String message) {
        return new ConfigValidationIssue(ConfigValidationIssue.Severity.ERROR, file, path, message);
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
