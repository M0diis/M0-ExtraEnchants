package me.m0dii.extraenchants.framework.config;

import me.m0dii.extraenchants.framework.model.ItemApplicability;
import me.m0dii.extraenchants.framework.runtime.TriggerType;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

final class CustomEnchantConfigValidator {
    private static final Pattern ID = Pattern.compile("[a-z0-9][a-z0-9_-]{0,63}");
    private static final Pattern COMPARISON = Pattern.compile("^([a-zA-Z0-9_]+)\\s*(<=|>=|==|!=|<|>)\\s*(.+)$");
    private static final Set<String> ROOT_KEYS = Set.of(
            "schema-version", "id", "display-name", "description", "rarity", "max-level",
            "enabled", "show-in-list", "weight", "spawn-chance", "category", "icon",
            "applicable-items", "conflicts-with", "metadata", "triggers"
    );
    private static final Set<String> TRIGGER_KEYS = Set.of(
            "chance", "cooldown", "global-cooldown", "target", "radius", "priority", "delay",
            "repeat", "repeat-interval", "cancel-event", "activation-limit", "chain-triggers",
            "conditions", "effects"
    );
    private static final Set<String> EFFECTS = Set.of(
            "damage", "true-damage", "heal", "lifesteal", "potion", "particle", "sound", "command",
            "message", "withdraw", "deposit", "stack-add", "stack-clear", "combo-reset"
    );
    private static final Set<String> TARGETS = Set.of(
            "SELF", "ATTACKER", "VICTIM", "PLAYERS", "MONSTERS", "ANIMALS", "ALL_ENTITIES",
            "NEARBY_ENTITIES", "ENEMIES"
    );
    private static final Set<String> CONDITIONS = Set.of(
            "victim_not_poisoned", "attacker_sneaking", "attacker_sprinting", "permission", "world",
            "region_allowed", "region_name", "balance_at_least", "stack_at_least", "combo_at_least"
    );
    private static final Set<String> VARIABLES = Set.of(
            "level", "random", "stack", "combo", "balance", "region_allowed", "attacker_health",
            "attacker_max_health", "victim_health", "victim_max_health", "distance", "radius", "damage",
            "health", "max_health", "sneaking"
    );

    List<ConfigValidationIssue> validate(YamlConfiguration yaml, String fileName) {
        List<ConfigValidationIssue> issues = new ArrayList<>();

        for (String key : new TreeSet<>(yaml.getKeys(false))) {
            if (!ROOT_KEYS.contains(key)) {
                error(issues, fileName, key, "Unknown top-level key");
            }
        }

        validateId(yaml.get("id"), fileName, issues);
        validateString(yaml, "display-name", fileName, issues, false);
        validateString(yaml, "description", fileName, issues, false);
        validateString(yaml, "rarity", fileName, issues, true);
        validateString(yaml, "category", fileName, issues, true);
        validateString(yaml, "icon", fileName, issues, true);
        validateInteger(yaml.get("max-level"), "max-level", fileName, issues, 1, 255, false);
        validateBoolean(yaml.get("enabled"), "enabled", fileName, issues);
        validateBoolean(yaml.get("show-in-list"), "show-in-list", fileName, issues);
        validateInteger(yaml.get("weight"), "weight", fileName, issues, 0, Integer.MAX_VALUE, false);
        validateInteger(yaml.get("spawn-chance"), "spawn-chance", fileName, issues, 0, Integer.MAX_VALUE, false);
        if (yaml.contains("weight") && yaml.contains("spawn-chance")) {
            warning(issues, fileName, "spawn-chance", "weight takes precedence when both keys are present");
        }

        validateApplicableItems(yaml.get("applicable-items"), fileName, issues);
        validateStringList(yaml.get("conflicts-with"), "conflicts-with", fileName, issues, false);
        validateMetadata(yaml.get("metadata"), fileName, issues);
        validateTriggers(yaml.getConfigurationSection("triggers"), fileName, issues);

        return issues;
    }

    private void validateId(Object raw, String fileName, List<ConfigValidationIssue> issues) {
        if (raw == null) {
            warning(issues, fileName, "id", "Missing id; the YAML file name will be used");
            return;
        }

        if (!(raw instanceof String id)) {
            error(issues, fileName, "id", "Expected a string");
            return;
        }

        String normalized = id.trim().toLowerCase(Locale.ROOT);
        if (!ID.matcher(normalized).matches()) {
            error(issues, fileName, "id", "Must match " + ID.pattern() + " and must not contain a namespace separator");
        }
    }

    private void validateApplicableItems(Object raw, String fileName, List<ConfigValidationIssue> issues) {
        if (!(raw instanceof List<?> values)) {
            error(issues, fileName, "applicable-items", "Expected a non-empty list of supported item categories");
            return;
        }

        if (values.isEmpty()) {
            error(issues, fileName, "applicable-items", "At least one supported item category is required");
            return;
        }

        Set<String> seen = new HashSet<>();
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            String path = "applicable-items[" + i + "]";
            if (!(value instanceof String item)) {
                error(issues, fileName, path, "Expected a string item category");
                continue;
            }

            String normalized = item.trim().toUpperCase(Locale.ROOT);
            if (normalized.isBlank()) {
                error(issues, fileName, path, "Item category cannot be blank");
            } else if (ItemApplicability.parse(normalized).isEmpty()) {
                error(issues, fileName, path, "Unknown item category '" + item + "'. Accepted values include: "
                        + String.join(", ", new TreeSet<>(ItemApplicability.acceptedNames())));
            } else if (!seen.add(normalized)) {
                warning(issues, fileName, path, "Duplicate item category '" + item + "'");
            }
        }
    }

    private void validateTriggers(ConfigurationSection triggers, String fileName, List<ConfigValidationIssue> issues) {
        if (triggers == null) {
            error(issues, fileName, "triggers", "A non-empty trigger section is required");
            return;
        }

        List<String> triggerNames = new ArrayList<>(triggers.getKeys(false));
        triggerNames.sort(String.CASE_INSENSITIVE_ORDER);
        if (triggerNames.isEmpty()) {
            error(issues, fileName, "triggers", "At least one trigger is required");
            return;
        }

        Set<String> canonicalNames = new HashSet<>();
        for (String triggerName : triggerNames) {
            String path = "triggers." + triggerName;
            TriggerType type = TriggerType.fromKey(triggerName);
            if (type == null) {
                error(issues, fileName, path, "Unknown trigger type");
            } else if (!canonicalNames.add(canonicalTriggerName(type))) {
                error(issues, fileName, path, "Duplicate trigger type using a different spelling");
            }

            ConfigurationSection section = triggers.getConfigurationSection(triggerName);
            if (section == null) {
                error(issues, fileName, path, "Expected a mapping of trigger options");
                continue;
            }

            validateTrigger(section, path, fileName, issues);
        }

        for (String triggerName : triggerNames) {
            ConfigurationSection section = triggers.getConfigurationSection(triggerName);
            if (section == null) {
                continue;
            }

            Object rawChains = section.get("chain-triggers");
            if (!(rawChains instanceof List<?> chains)) {
                continue;
            }

            for (int i = 0; i < chains.size(); i++) {
                Object rawChain = chains.get(i);
                if (!(rawChain instanceof String chain)) {
                    continue;
                }

                TriggerType chainType = TriggerType.fromKey(chain);
                if (chainType == null || !canonicalNames.contains(canonicalTriggerName(chainType))) {
                    error(issues, fileName, "triggers." + triggerName + ".chain-triggers[" + i + "]",
                            "Referenced trigger does not exist: " + chain);
                }
            }
        }
    }

    private void validateTrigger(ConfigurationSection section, String path, String fileName,
                                 List<ConfigValidationIssue> issues) {
        for (String key : new TreeSet<>(section.getKeys(false))) {
            if (!TRIGGER_KEYS.contains(key)) {
                error(issues, fileName, path + "." + key, "Unknown trigger option");
            }
        }

        validateExpression(section.get("chance"), path + ".chance", fileName, issues, "100", true);
        validateExpression(section.get("cooldown"), path + ".cooldown", fileName, issues, "0", true);
        validateExpression(section.get("global-cooldown"), path + ".global-cooldown", fileName, issues, "0", true);
        validateExpression(section.get("radius"), path + ".radius", fileName, issues, "6", true);

        Object target = section.get("target");
        if (target != null && (!(target instanceof String) || !TARGETS.contains(String.valueOf(target).toUpperCase(Locale.ROOT)))) {
            error(issues, fileName, path + ".target", "Unknown target; accepted values: " + String.join(", ", TARGETS));
        }

        validateInteger(section.get("priority"), path + ".priority", fileName, issues,
                Integer.MIN_VALUE, Integer.MAX_VALUE, false);
        validateInteger(section.get("delay"), path + ".delay", fileName, issues, 0, Integer.MAX_VALUE, false);
        validateInteger(section.get("repeat"), path + ".repeat", fileName, issues, 1, Integer.MAX_VALUE, false);
        validateInteger(section.get("repeat-interval"), path + ".repeat-interval", fileName, issues, 1, Integer.MAX_VALUE, false);
        validateInteger(section.get("activation-limit"), path + ".activation-limit", fileName, issues, -1, Integer.MAX_VALUE, false);
        validateBoolean(section.get("cancel-event"), path + ".cancel-event", fileName, issues);
        validateStringList(section.get("chain-triggers"), path + ".chain-triggers", fileName, issues, false);

        Object conditions = section.get("conditions");
        if (conditions != null) {
            validateCondition(conditions, path + ".conditions", fileName, issues);
        }

        Object effects = section.get("effects");
        if (!(effects instanceof List<?> effectRows) || effectRows.isEmpty()) {
            error(issues, fileName, path + ".effects", "A non-empty list of effects is required");
        } else {
            validateEffects(effectRows, path + ".effects", fileName, issues);
        }
    }

    private void validateEffects(List<?> rows, String path, String fileName, List<ConfigValidationIssue> issues) {
        for (int i = 0; i < rows.size(); i++) {
            Object row = rows.get(i);
            String rowPath = path + "[" + i + "]";
            if (!(row instanceof Map<?, ?> map) || map.size() != 1) {
                error(issues, fileName, rowPath, "Each effect row must contain exactly one effect mapping");
                continue;
            }

            Map.Entry<?, ?> entry = map.entrySet().iterator().next();
            if (!(entry.getKey() instanceof String rawType)) {
                error(issues, fileName, rowPath, "Effect name must be a string");
                continue;
            }

            String type = rawType.trim().toLowerCase(Locale.ROOT).replace('_', '-');
            if (!EFFECTS.contains(type)) {
                error(issues, fileName, rowPath + "." + rawType, "Unknown effect type");
                continue;
            }

            validateEffectValue(type, entry.getValue(), rowPath + "." + rawType, fileName, issues);
        }
    }

    private void validateEffectValue(String type, Object value, String path, String fileName,
                                     List<ConfigValidationIssue> issues) {
        switch (type) {
            case "damage", "true-damage", "heal", "lifesteal", "withdraw", "deposit" ->
                    validateExpression(value, path, fileName, issues, null, false);
            case "command", "message" -> validateStringValue(value, path, fileName, issues, true);
            case "sound" -> validateSound(value, path, fileName, issues);
            case "combo-reset", "stack-clear" -> {
                if (!(value instanceof Boolean)) {
                    warning(issues, fileName, path, "This effect is normally configured as true");
                }
            }
            case "stack-add" -> {
                if (value instanceof Map<?, ?> map) {
                    validateMapKeys(map, Set.of("amount", "duration"), path, fileName, issues);
                    validateExpression(map.get("amount"), path + ".amount", fileName, issues, "1", true);
                    validateExpression(map.get("duration"), path + ".duration", fileName, issues, "200", true);
                } else {
                    validateExpression(value, path, fileName, issues, null, false);
                }
            }
            case "potion" -> validatePotion(value, path, fileName, issues);
            case "particle" -> validateParticle(value, path, fileName, issues);
            default -> error(issues, fileName, path, "Unsupported effect type");
        }
    }

    private void validatePotion(Object value, String path, String fileName, List<ConfigValidationIssue> issues) {
        if (!(value instanceof Map<?, ?> map)) {
            error(issues, fileName, path, "Potion effect expects a mapping");
            return;
        }

        validateMapKeys(map, Set.of("type", "duration", "amplifier"), path, fileName, issues);
        validateStringValue(map.get("type"), path + ".type", fileName, issues, true);
        validateExpression(map.get("duration"), path + ".duration", fileName, issues, "60", true);
        validateExpression(map.get("amplifier"), path + ".amplifier", fileName, issues, "0", true);
    }

    private void validateParticle(Object value, String path, String fileName, List<ConfigValidationIssue> issues) {
        if (!(value instanceof Map<?, ?> map)) {
            error(issues, fileName, path, "Particle effect expects a mapping");
            return;
        }

        validateMapKeys(map, Set.of("type", "count"), path, fileName, issues);
        Object type = map.get("type");
        validateStringValue(type, path + ".type", fileName, issues, true);
        if (type instanceof String name) {
            try {
                Particle.valueOf(name.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                error(issues, fileName, path + ".type", "Unknown particle type: " + name);
            }
        }
        validateInteger(map.get("count"), path + ".count", fileName, issues, 0, Integer.MAX_VALUE, true);
    }

    private void validateSound(Object value, String path, String fileName, List<ConfigValidationIssue> issues) {
        validateStringValue(value, path, fileName, issues, true);
        if (value instanceof String sound
                && NamespacedKey.fromString(sound.trim().toLowerCase(Locale.ROOT)) == null) {
            error(issues, fileName, path, "Invalid sound key: " + sound);
        }
    }

    private void validateCondition(Object raw, String path, String fileName, List<ConfigValidationIssue> issues) {
        if (raw instanceof String line) {
            String trimmed = line.trim();
            if (CONDITIONS.contains(trimmed.toLowerCase(Locale.ROOT))) {
                return;
            }

            var matcher = COMPARISON.matcher(trimmed);
            if (!matcher.matches()) {
                error(issues, fileName, path, "Unknown condition or invalid comparison");
                return;
            }

            String variable = matcher.group(1).toLowerCase(Locale.ROOT);
            if (!VARIABLES.contains(variable)) {
                error(issues, fileName, path, "Unknown condition variable: " + variable);
            }
            validateExpression(matcher.group(3), path, fileName, issues, null, false);
            return;
        }

        if (raw instanceof List<?> list) {
            if (list.isEmpty()) {
                error(issues, fileName, path, "Condition list cannot be empty");
            }
            for (int i = 0; i < list.size(); i++) {
                validateCondition(list.get(i), path + "[" + i + "]", fileName, issues);
            }
            return;
        }

        if (raw instanceof ConfigurationSection section) {
            validateConditionMap(section.getValues(false), path, fileName, issues);
            return;
        }

        if (raw instanceof Map<?, ?> map) {
            validateConditionMap(map, path, fileName, issues);
            return;
        }

        error(issues, fileName, path, "Expected a condition string, list, or mapping");
    }

    private void validateConditionMap(Map<?, ?> map, String path, String fileName, List<ConfigValidationIssue> issues) {
        if (map.isEmpty()) {
            error(issues, fileName, path, "Condition mapping cannot be empty");
            return;
        }

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String rawKey)) {
                error(issues, fileName, path, "Condition name must be a string");
                continue;
            }

            String key = rawKey.toLowerCase(Locale.ROOT);
            String childPath = path + "." + rawKey;
            if (key.equals("all") || key.equals("any")) {
                if (!(entry.getValue() instanceof List<?>)) {
                    error(issues, fileName, childPath, "Expected a list of conditions");
                } else {
                    validateCondition(entry.getValue(), childPath, fileName, issues);
                }
            } else if (key.equals("not")) {
                validateCondition(entry.getValue(), childPath, fileName, issues);
            } else if (!CONDITIONS.contains(key)) {
                error(issues, fileName, childPath, "Unknown condition");
            } else if (key.equals("permission") || key.equals("world") || key.equals("region_name")) {
                validateStringOrStringList(entry.getValue(), childPath, fileName, issues);
            } else if (key.equals("balance_at_least") || key.equals("stack_at_least") || key.equals("combo_at_least")) {
                validateExpression(entry.getValue(), childPath, fileName, issues, null, false);
            }
        }
    }

    private void validateMapKeys(Map<?, ?> map, Set<String> allowed, String path, String fileName,
                                 List<ConfigValidationIssue> issues) {
        for (Object key : map.keySet()) {
            if (!(key instanceof String stringKey) || !allowed.contains(stringKey)) {
                error(issues, fileName, path + "." + key, "Unknown mapping key");
            }
        }
    }

    private void validateMetadata(Object raw, String fileName, List<ConfigValidationIssue> issues) {
        if (raw == null) {
            return;
        }
        if (!(raw instanceof ConfigurationSection) && !(raw instanceof Map<?, ?>)) {
            error(issues, fileName, "metadata", "Expected a mapping");
        }
    }

    private void validateString(YamlConfiguration yaml, String key, String fileName,
                                List<ConfigValidationIssue> issues, boolean nonBlank) {
        if (!yaml.contains(key)) {
            return;
        }
        validateStringValue(yaml.get(key), key, fileName, issues, nonBlank);
    }

    private void validateStringValue(Object value, String path, String fileName,
                                     List<ConfigValidationIssue> issues, boolean nonBlank) {
        if (!(value instanceof String string)) {
            error(issues, fileName, path, "Expected a string");
        } else if (nonBlank && string.isBlank()) {
            error(issues, fileName, path, "Value cannot be blank");
        }
    }

    private void validateStringOrStringList(Object raw, String path, String fileName,
                                            List<ConfigValidationIssue> issues) {
        if (raw instanceof String string) {
            if (string.isBlank()) {
                error(issues, fileName, path, "Value cannot be blank");
            }
            return;
        }
        if (raw instanceof List<?> list) {
            validateStringList(list, path, fileName, issues, true);
            return;
        }
        error(issues, fileName, path, "Expected a string or list of strings");
    }

    private void validateStringList(Object raw, String path, String fileName,
                                    List<ConfigValidationIssue> issues, boolean required) {
        if (raw == null && !required) {
            return;
        }
        if (!(raw instanceof List<?> list)) {
            error(issues, fileName, path, "Expected a list of strings");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof String value) || value.isBlank()) {
                error(issues, fileName, path + "[" + i + "]", "Expected a non-blank string");
            }
        }
    }

    private void validateInteger(Object raw, String path, String fileName, List<ConfigValidationIssue> issues,
                                 int minimum, int maximum, boolean optionalWithDefault) {
        if (raw == null && optionalWithDefault) {
            return;
        }
        if (raw == null) {
            return;
        }
        if (!(raw instanceof Number number)
                || number.doubleValue() != Math.rint(number.doubleValue())
                || number.doubleValue() < minimum
                || number.doubleValue() > maximum) {
            error(issues, fileName, path, "Expected an integer between " + minimum + " and " + maximum);
        }
    }

    private void validateBoolean(Object raw, String path, String fileName, List<ConfigValidationIssue> issues) {
        if (raw != null && !(raw instanceof Boolean)) {
            error(issues, fileName, path, "Expected true or false");
        }
    }

    private void validateExpression(Object raw, String path, String fileName, List<ConfigValidationIssue> issues,
                                    String defaultValue, boolean optionalWithDefault) {
        if (raw == null) {
            if (optionalWithDefault || defaultValue == null) {
                return;
            }
            error(issues, fileName, path, "A formula is required");
            return;
        }

        if (raw instanceof Number number) {
            if (!Double.isFinite(number.doubleValue())) {
                error(issues, fileName, path, "Formula value must be finite");
            }
            return;
        }
        if (!(raw instanceof String expression) || expression.isBlank()) {
            error(issues, fileName, path, "Expected a numeric value or formula string");
            return;
        }

        try {
            new ExpressionBuilder(expression).variables(VARIABLES).build();
        } catch (RuntimeException ex) {
            error(issues, fileName, path, "Invalid formula: " + expression);
        }
    }

    private String canonicalTriggerName(TriggerType type) {
        String[] parts = type.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            result.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        }
        return result.toString();
    }

    private void error(List<ConfigValidationIssue> issues, String fileName, String path, String message) {
        issues.add(new ConfigValidationIssue(ConfigValidationIssue.Severity.ERROR, fileName, path, message));
    }

    private void warning(List<ConfigValidationIssue> issues, String fileName, String path, String message) {
        issues.add(new ConfigValidationIssue(ConfigValidationIssue.Severity.WARNING, fileName, path, message));
    }
}
