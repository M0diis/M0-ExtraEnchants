package me.m0dii.extraenchants.framework.runtime;

import me.m0dii.extraenchants.framework.api.TargetSelector;
import me.m0dii.extraenchants.framework.registry.TargetRegistry;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Resolves configured target selectors and keeps target-specific logic out of the dispatcher. */
public final class TargetingService {
    private final TargetRegistry registry;
    private final Exp4jFormulaEngine formulaEngine;

    public TargetingService(TargetRegistry registry, Exp4jFormulaEngine formulaEngine) {
        this.registry = registry;
        this.formulaEngine = formulaEngine;
        registerDefaults();
    }

    public Collection<LivingEntity> resolve(String targetType, ExecutionContext context, String radiusExpression) {
        TargetSelector selector = registry.get(targetType);
        if (selector == null) {
            return List.of();
        }

        double radius = Math.max(1D, formulaEngine.evaluate(radiusExpression, context.getVariables()));
        context.getVariables().put("radius", radius);
        return selector.select(context, radius);
    }

    private void registerDefaults() {
        registry.register("SELF", (context, radius) -> context.getOwner() == null
                ? List.of() : List.of(context.getOwner()));
        registry.register("ATTACKER", (context, radius) -> context.getAttacker() == null
                ? List.of() : List.of(context.getAttacker()));
        registry.register("VICTIM", (context, radius) -> context.getVictim() == null
                ? List.of() : List.of(context.getVictim()));

        registry.register("PLAYERS", (context, radius) -> nearby(context, radius).stream()
                .filter(entity -> entity instanceof Player)
                .toList());
        registry.register("MONSTERS", (context, radius) -> nearby(context, radius).stream()
                .filter(entity -> entity instanceof Monster)
                .toList());
        registry.register("ANIMALS", (context, radius) -> nearby(context, radius).stream()
                .filter(entity -> entity instanceof Animals)
                .toList());
        registry.register("ALL_ENTITIES", this::nearby);
        registry.register("NEARBY_ENTITIES", this::nearby);
        registry.register("ENEMIES", (context, radius) -> nearby(context, radius).stream()
                .filter(entity -> !(entity instanceof Animals))
                .filter(entity -> context.getOwner() == null
                        || !entity.getUniqueId().equals(context.getOwner().getUniqueId()))
                .toList());
    }

    private Collection<LivingEntity> nearby(ExecutionContext context, Object radiusValue) {
        if (context.getLocation() == null || context.getLocation().getWorld() == null) {
            return List.of();
        }

        double radius = radiusValue instanceof Number number ? Math.max(0D, number.doubleValue()) : 6D;
        var location = context.getLocation();
        return location.getWorld().getNearbyLivingEntities(location, radius, radius, radius).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(location)))
                .toList();
    }
}
