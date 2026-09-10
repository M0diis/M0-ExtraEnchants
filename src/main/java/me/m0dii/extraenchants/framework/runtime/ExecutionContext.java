package me.m0dii.extraenchants.framework.runtime;

import lombok.Getter;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.framework.model.CustomEnchantDefinition;
import me.m0dii.extraenchants.framework.model.TriggerDefinition;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutionContext {
    public static final int MAX_CHAIN_DEPTH = 4;

    @Getter
    private final ExtraEnchants plugin;
    @Getter
    private final CustomEnchantDefinition enchant;
    @Getter
    private final TriggerDefinition trigger;
    @Getter
    private final TriggerType triggerType;
    @Getter
    private final Event event;
    @Getter
    private final LivingEntity attacker;
    @Getter
    private final LivingEntity victim;
    @Getter
    private final Player owner;
    @Getter
    private final int level;
    private final Location location;
    @Getter
    private final Map<String, Double> variables;
    @Getter
    private int chainDepth = 0;
    private final Set<String> chainPath;

    public ExecutionContext(
            ExtraEnchants plugin,
            CustomEnchantDefinition enchant,
            TriggerDefinition trigger,
            TriggerType triggerType,
            Event event,
            LivingEntity attacker,
            LivingEntity victim,
            Player owner,
            int level,
            Location location
    ) {
        this(
                plugin,
                enchant,
                trigger,
                triggerType,
                event,
                attacker,
                victim,
                owner,
                level,
                location,
                0,
                rootPath(enchant, trigger),
                Map.of()
        );
    }

    private ExecutionContext(
            ExtraEnchants plugin,
            CustomEnchantDefinition enchant,
            TriggerDefinition trigger,
            TriggerType triggerType,
            Event event,
            LivingEntity attacker,
            LivingEntity victim,
            Player owner,
            int level,
            Location location,
            int chainDepth,
            Set<String> chainPath,
            Map<String, Double> variables
    ) {
        this.plugin = plugin;
        this.enchant = enchant;
        this.trigger = trigger;
        this.triggerType = triggerType;
        this.event = event;
        this.attacker = attacker;
        this.victim = victim;
        this.owner = owner;
        this.level = level;
        this.location = location == null ? null : location.clone();
        this.chainDepth = Math.max(0, chainDepth);
        this.chainPath = Set.copyOf(new LinkedHashSet<>(chainPath));
        this.variables = new ConcurrentHashMap<>(variables);
    }

    public Location getLocation() {
        return location == null ? null : location.clone();
    }

    public UUID getOwnerId() {
        return owner == null ? null : owner.getUniqueId();
    }

    /**
     * Retained for source compatibility with integrations written against the
     * original mutable context. New runtime code should create child contexts
     * with {@link #createChildContext(CustomEnchantDefinition, TriggerDefinition, TriggerType)}.
     */
    public void incrementChainDepth() {
        if (chainDepth < MAX_CHAIN_DEPTH) {
            this.chainDepth++;
        }
    }

    public Set<String> getChainPath() {
        return chainPath;
    }

    public boolean hasVisited(CustomEnchantDefinition definition, TriggerDefinition trigger) {
        return chainPath.contains(chainKey(definition, trigger));
    }

    /**
     * Creates a child context for a chained trigger. The child inherits the
     * event participants and variable snapshot, while carrying the complete
     * chain path so recursive configurations cannot loop forever.
     *
     * @return a child context, or {@code null} when the depth limit or cycle
     *         guard rejects the transition
     */
    public ExecutionContext createChildContext(
            CustomEnchantDefinition childEnchant,
            TriggerDefinition childTrigger,
            TriggerType childTriggerType
    ) {
        if (childEnchant == null || childTrigger == null || childTriggerType == null) {
            return null;
        }

        int nextDepth = chainDepth + 1;
        if (nextDepth > MAX_CHAIN_DEPTH) {
            return null;
        }

        String childKey = chainKey(childEnchant, childTrigger);
        if (chainPath.contains(childKey)) {
            return null;
        }

        Set<String> nextPath = new LinkedHashSet<>(chainPath);
        nextPath.add(childKey);
        return new ExecutionContext(
                plugin,
                childEnchant,
                childTrigger,
                childTriggerType,
                event,
                attacker,
                victim,
                owner,
                level,
                location,
                nextDepth,
                nextPath,
                variables
        );
    }

    /**
     * Returns an isolated snapshot for a delayed/repeated task. A repeat must
     * not share mutable variables with another repeat running on a region.
     */
    public ExecutionContext snapshot() {
        return new ExecutionContext(
                plugin,
                enchant,
                trigger,
                triggerType,
                event,
                attacker,
                victim,
                owner,
                level,
                location,
                chainDepth,
                chainPath,
                variables
        );
    }

    private static Set<String> rootPath(CustomEnchantDefinition enchant, TriggerDefinition trigger) {
        if (enchant == null || trigger == null) {
            return Set.of();
        }

        return Set.of(chainKey(enchant, trigger));
    }

    private static String chainKey(CustomEnchantDefinition enchant, TriggerDefinition trigger) {
        String enchantId = enchant == null || enchant.getId() == null ? "unknown" : enchant.getId();
        String triggerName = trigger == null || trigger.getName() == null ? "unknown" : trigger.getName();
        return enchantId.trim().toLowerCase(Locale.ROOT) + ":" + triggerName.trim().toLowerCase(Locale.ROOT);
    }
}

