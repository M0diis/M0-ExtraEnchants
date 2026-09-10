package me.m0dii.extraenchants.framework.runtime;

import me.m0dii.extraenchants.framework.api.Condition;
import me.m0dii.extraenchants.framework.hooks.RegionHook;
import me.m0dii.extraenchants.framework.registry.ConditionRegistry;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Locale;

/** Evaluates conditions and owns the built-in condition implementations. */
public final class ConditionService {
    private static final long COMBO_WINDOW_MILLIS = 2_000L;

    private final ConditionRegistry registry;
    private final Exp4jFormulaEngine formulaEngine;
    private final RegionHook regionHook;
    private final EconomyService economyService;
    private final StatusStackService statusStackService;
    private final ComboStateService comboStateService;

    public ConditionService(
            ConditionRegistry registry,
            Exp4jFormulaEngine formulaEngine,
            RegionHook regionHook,
            EconomyService economyService,
            StatusStackService statusStackService,
            ComboStateService comboStateService
    ) {
        this.registry = registry;
        this.formulaEngine = formulaEngine;
        this.regionHook = regionHook;
        this.economyService = economyService;
        this.statusStackService = statusStackService;
        this.comboStateService = comboStateService;
        registerDefaults();
    }

    public boolean pass(List<Condition> conditions, ExecutionContext context, LivingEntity target) {
        for (Condition condition : conditions) {
            if (!condition.test(context, target)) {
                return false;
            }
        }
        return true;
    }

    private void registerDefaults() {
        registry.register("victim_not_poisoned", ignored -> (context, target) ->
                context.getVictim() == null || !context.getVictim().hasPotionEffect(PotionEffectType.POISON));
        registry.register("attacker_sneaking", ignored -> (context, target) ->
                context.getAttacker() instanceof Player player && player.isSneaking());
        registry.register("attacker_sprinting", ignored -> (context, target) ->
                context.getAttacker() instanceof Player player && player.isSprinting());
        registry.register("permission", value -> (context, target) ->
                context.getOwner() != null && context.getOwner().hasPermission(String.valueOf(value)));
        registry.register("world", value -> {
            List<String> worlds = value instanceof List<?> list
                    ? list.stream().map(String::valueOf)
                    .map(item -> item.toLowerCase(Locale.ROOT)).toList()
                    : List.of(String.valueOf(value).toLowerCase(Locale.ROOT));
            return (context, target) -> context.getLocation() != null
                    && context.getLocation().getWorld() != null
                    && worlds.contains(context.getLocation().getWorld().getName().toLowerCase(Locale.ROOT));
        });
        registry.register("region_allowed", ignored -> (context, target) ->
                regionHook.isAllowed(context.getOwner(), context.getLocation()));
        registry.register("region_name", value -> {
            List<String> regions = value instanceof List<?> list
                    ? list.stream().map(String::valueOf).map(item -> item.toLowerCase(Locale.ROOT)).toList()
                    : List.of(String.valueOf(value).toLowerCase(Locale.ROOT));
            return (context, target) -> {
                String current = regionHook.getRegionName(context.getLocation());
                return !current.isBlank() && regions.contains(current.toLowerCase(Locale.ROOT));
            };
        });
        registry.register("balance_at_least", value -> (context, target) -> {
            if (context.getOwner() == null) {
                return false;
            }
            double required = value instanceof Number number
                    ? number.doubleValue()
                    : formulaEngine.evaluate(String.valueOf(value), context.getVariables());
            return economyService.has(context.getOwner(), required);
        });
        registry.register("stack_at_least", value -> (context, target) ->
                statusStackService.get(context.getOwnerId(), stackKey(context)) >= evaluateAmount(value, context));
        registry.register("combo_at_least", value -> (context, target) ->
                comboStateService.getHits(
                        context.getOwnerId(), comboKey(context), COMBO_WINDOW_MILLIS) >= evaluateAmount(value, context));
    }

    private int evaluateAmount(Object value, ExecutionContext context) {
        return value instanceof Number number
                ? number.intValue()
                : (int) Math.round(formulaEngine.evaluate(String.valueOf(value), context.getVariables()));
    }

    private String stackKey(ExecutionContext context) {
        String ownerId = context.getOwnerId() == null ? "none" : context.getOwnerId().toString();
        return context.getEnchant().getId() + ":" + ownerId;
    }

    private String comboKey(ExecutionContext context) {
        String attackerId = context.getAttacker() == null ? "none" : context.getAttacker().getUniqueId().toString();
        String victimId = context.getVictim() == null ? "none" : context.getVictim().getUniqueId().toString();
        return context.getEnchant().getId() + ":" + attackerId + ":" + victimId;
    }
}
