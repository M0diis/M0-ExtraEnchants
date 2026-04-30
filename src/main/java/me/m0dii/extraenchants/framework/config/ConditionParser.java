package me.m0dii.extraenchants.framework.config;

import me.m0dii.extraenchants.framework.api.Condition;
import me.m0dii.extraenchants.framework.registry.ConditionRegistry;
import me.m0dii.extraenchants.framework.runtime.ExecutionContext;
import me.m0dii.extraenchants.framework.runtime.Exp4jFormulaEngine;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionParser {
    private static final Pattern COMPARISON = Pattern.compile("^([a-zA-Z0-9_]+)\\s*(<=|>=|==|!=|<|>)\\s*(.+)$");

    private final ConditionRegistry conditionRegistry;
    private final Exp4jFormulaEngine formulaEngine;

    public ConditionParser(ConditionRegistry conditionRegistry, Exp4jFormulaEngine formulaEngine) {
        this.conditionRegistry = conditionRegistry;
        this.formulaEngine = formulaEngine;
    }

    public Condition parse(Object raw) {
        if (raw == null) {
            return (context, target) -> true;
        }

        if (raw instanceof String line) {
            return parseString(line);
        }

        if (raw instanceof List<?> list) {
            List<Condition> children = new ArrayList<>();
            for (Object node : list) {
                children.add(parse(node));
            }

            return (context, target) -> children.stream().allMatch(c -> c.test(context, target));
        }

        if (raw instanceof ConfigurationSection section) {
            return parseMap(section.getValues(false));
        }

        if (raw instanceof Map<?, ?> map) {
            return parseMap((Map<String, Object>) map);
        }

        return (context, target) -> true;
    }

    private Condition parseMap(Map<String, Object> map) {
        if (map.containsKey("all")) {
            List<?> list = asList(map.get("all"));
            List<Condition> children = list.stream().map(this::parse).toList();
            return (context, target) -> children.stream().allMatch(c -> c.test(context, target));
        }

        if (map.containsKey("any")) {
            List<?> list = asList(map.get("any"));
            List<Condition> children = list.stream().map(this::parse).toList();
            return (context, target) -> children.stream().anyMatch(c -> c.test(context, target));
        }

        if (map.containsKey("not")) {
            Condition child = parse(map.get("not"));
            return (context, target) -> !child.test(context, target);
        }

        List<Condition> joined = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Condition registered = conditionRegistry.build(entry.getKey(), entry.getValue());
            if (registered != null) {
                joined.add(registered);
            }
        }

        return (context, target) -> joined.stream().allMatch(c -> c.test(context, target));
    }

    private List<?> asList(Object value) {
        if (value instanceof List<?> list) {
            return list;
        }

        return List.of(value);
    }

    private Condition parseString(String line) {
        String trimmed = line.trim();
        Condition registered = conditionRegistry.build(trimmed, true);
        if (registered != null) {
            return registered;
        }

        Matcher matcher = COMPARISON.matcher(trimmed);
        if (!matcher.matches()) {
            return (context, target) -> true;
        }

        String left = matcher.group(1).toLowerCase();
        String operator = matcher.group(2);
        String right = matcher.group(3);

        return (context, target) -> {
            double lhs = resolveVariable(left, context, target);
            double rhs = formulaEngine.evaluate(right, context.getVariables());

            return switch (operator) {
                case ">" -> lhs > rhs;
                case "<" -> lhs < rhs;
                case ">=" -> lhs >= rhs;
                case "<=" -> lhs <= rhs;
                case "==" -> lhs == rhs;
                case "!=" -> lhs != rhs;
                default -> true;
            };
        };
    }

    private double resolveVariable(String key, ExecutionContext context, LivingEntity target) {
        if (context.getVariables().containsKey(key)) {
            return context.getVariables().get(key);
        }

        return switch (key) {
            case "level" -> context.getLevel();
            case "attacker_health" -> context.getAttacker() == null ? 0D : context.getAttacker().getHealth();
            case "victim_health" -> context.getVictim() == null ? 0D : context.getVictim().getHealth();
            case "max_health" -> target == null ? 0D : target.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
            case "health" -> target == null ? 0D : target.getHealth();
            case "distance" -> context.getAttacker() == null || context.getVictim() == null ? 0D : context.getAttacker().getLocation().distance(context.getVictim().getLocation());
            case "sneaking" -> context.getAttacker() instanceof Player p && p.isSneaking() ? 1D : 0D;
            default -> 0D;
        };
    }
}

