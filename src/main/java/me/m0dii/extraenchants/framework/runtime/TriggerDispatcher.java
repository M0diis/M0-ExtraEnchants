package me.m0dii.extraenchants.framework.runtime;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.framework.hooks.RegionHook;
import me.m0dii.extraenchants.framework.model.CustomEnchantDefinition;
import me.m0dii.extraenchants.framework.model.TriggerDefinition;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/** Coordinates chance, conditions, state gates, scheduling and trigger chains. */
public final class TriggerDispatcher {
    private static final long COMBO_WINDOW_MILLIS = 2_000L;

    private final ExtraEnchants plugin;
    private final Exp4jFormulaEngine formulaEngine;
    private final ConditionService conditionService;
    private final TargetingService targetingService;
    private final EffectService effectService;
    private final CooldownService cooldownService;
    private final ActivationService activationService;
    private final ComboStateService comboStateService;
    private final StatusStackService statusStackService;
    private final EconomyService economyService;
    private final RegionHook regionHook;
    private final TriggerTaskScheduler scheduler;
    private final Consumer<String> debug;

    public TriggerDispatcher(
            ExtraEnchants plugin,
            Exp4jFormulaEngine formulaEngine,
            ConditionService conditionService,
            TargetingService targetingService,
            EffectService effectService,
            CooldownService cooldownService,
            ActivationService activationService,
            ComboStateService comboStateService,
            StatusStackService statusStackService,
            EconomyService economyService,
            RegionHook regionHook,
            TriggerTaskScheduler scheduler,
            Consumer<String> debug
    ) {
        this.plugin = plugin;
        this.formulaEngine = formulaEngine;
        this.conditionService = conditionService;
        this.targetingService = targetingService;
        this.effectService = effectService;
        this.cooldownService = cooldownService;
        this.activationService = activationService;
        this.comboStateService = comboStateService;
        this.statusStackService = statusStackService;
        this.economyService = economyService;
        this.regionHook = regionHook;
        this.scheduler = scheduler;
        this.debug = debug == null ? ignored -> { } : debug;
    }

    public void dispatch(ExecutionContext context) {
        if (context == null || context.getEnchant() == null || context.getTrigger() == null
                || context.getTriggerType() == null) {
            return;
        }

        if (context.getTriggerType() == TriggerType.ON_ATTACK) {
            int combo = comboStateService.registerHit(
                    context.getOwnerId(), comboKey(context), COMBO_WINDOW_MILLIS);
            context.getVariables().put("combo", (double) combo);
        }

        fillVariables(context);

        TriggerDefinition trigger = context.getTrigger();
        double chance = Math.clamp(formulaEngine.evaluate(trigger.getChance(), context.getVariables()), 0D, 100D);
        if (ThreadLocalRandom.current().nextDouble(0D, 100D) > chance) {
            debug.accept("Chance check failed for " + context.getEnchant().getId() + " "
                    + context.getTriggerType() + " chance=" + chance);
            return;
        }

        String cooldownKey = cooldownKey(context);
        long playerCooldown = secondsToMillis(trigger.getCooldown(), context);
        long globalCooldown = secondsToMillis(trigger.getGlobalCooldown(), context);

        CooldownService.Reservation reservation = cooldownService.tryReserve(
                context.getOwnerId(), cooldownKey, playerCooldown, globalCooldown);
        if (reservation == null) {
            debug.accept("Cooldown active for " + context.getEnchant().getId() + " key=" + cooldownKey);
            return;
        }

        if (!conditionService.pass(trigger.getConditions(), context, context.getVictim())) {
            cooldownService.release(reservation);
            debug.accept("Conditions failed for " + context.getEnchant().getId()
                    + " trigger=" + context.getTriggerType());
            return;
        }

        if (!activationService.tryActivate(context.getOwnerId(), cooldownKey, trigger.getActivationLimit())) {
            cooldownService.release(reservation);
            return;
        }

        if (trigger.isCancelEvent() && context.getEvent() instanceof Cancellable cancellable) {
            cancellable.setCancelled(true);
        }

        int delay = Math.max(0, trigger.getDelayTicks());
        int repeat = Math.max(1, trigger.getRepeatCount());
        int interval = Math.max(1, trigger.getRepeatIntervalTicks());
        for (int i = 0; i < repeat; i++) {
            ExecutionContext scheduledContext = context.snapshot();
            long scheduleDelay = delay + (long) i * interval;
            scheduler.schedule(scheduledContext.getLocation(),
                    () -> executeScheduled(scheduledContext), scheduleDelay);
        }
    }

    private void executeScheduled(ExecutionContext context) {
        var targets = targetingService.resolve(
                context.getTrigger().getTarget(), context, context.getTrigger().getRadius());
        effectService.executeAll(context, targets);

        for (String chainedName : context.getTrigger().getChainTriggers()) {
            TriggerDefinition chained = findTrigger(context.getEnchant(), chainedName);
            TriggerType chainedType = TriggerType.fromKey(chainedName);
            if (chained == null || chainedType == null) {
                debug.accept("Skipped unknown chained trigger " + chainedName
                        + " for " + context.getEnchant().getId());
                continue;
            }

            ExecutionContext child = context.createChildContext(context.getEnchant(), chained, chainedType);
            if (child != null) {
                dispatch(child);
            } else {
                debug.accept("Skipped chained trigger " + chainedName + " at depth/cycle guard for "
                        + context.getEnchant().getId());
            }
        }
    }

    private void fillVariables(ExecutionContext context) {
        context.getVariables().put("level", (double) context.getLevel());
        context.getVariables().put("random", ThreadLocalRandom.current().nextDouble(100D));
        context.getVariables().put("stack", (double) statusStackService.get(
                context.getOwnerId(), stackKey(context)));
        context.getVariables().put("combo", (double) comboStateService.getHits(
                context.getOwnerId(), comboKey(context), COMBO_WINDOW_MILLIS));
        context.getVariables().put("balance", context.getOwner() == null
                ? 0D : economyService.balance(context.getOwner()));
        context.getVariables().put("region_allowed",
                regionHook.isAllowed(context.getOwner(), context.getLocation()) ? 1D : 0D);

        if (context.getAttacker() != null) {
            context.getVariables().put("attacker_health", context.getAttacker().getHealth());
            if (context.getAttacker().getAttribute(Attribute.MAX_HEALTH) != null) {
                context.getVariables().put("attacker_max_health",
                        context.getAttacker().getAttribute(Attribute.MAX_HEALTH).getValue());
            }
        }
        if (context.getVictim() != null) {
            context.getVariables().put("victim_health", context.getVictim().getHealth());
            if (context.getVictim().getAttribute(Attribute.MAX_HEALTH) != null) {
                context.getVariables().put("victim_max_health",
                        context.getVictim().getAttribute(Attribute.MAX_HEALTH).getValue());
            }
        }
        if (context.getAttacker() != null && context.getVictim() != null
                && context.getAttacker().getWorld() == context.getVictim().getWorld()) {
            context.getVariables().put("distance", context.getAttacker().getLocation()
                    .distance(context.getVictim().getLocation()));
        }
    }

    private TriggerDefinition findTrigger(CustomEnchantDefinition enchant, String name) {
        if (name == null) {
            return null;
        }
        TriggerDefinition direct = enchant.getTriggers().get(name);
        if (direct != null) {
            return direct;
        }
        return enchant.getTriggers().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(java.util.Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private long secondsToMillis(String expression, ExecutionContext context) {
        long seconds = Math.max(0L, formulaEngine.evaluateInt(expression, context.getVariables()));
        return seconds >= Long.MAX_VALUE / 1_000L ? Long.MAX_VALUE : seconds * 1_000L;
    }

    private String cooldownKey(ExecutionContext context) {
        return context.getEnchant().getId().toLowerCase(Locale.ROOT) + ":"
                + context.getTrigger().getName().toLowerCase(Locale.ROOT);
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
