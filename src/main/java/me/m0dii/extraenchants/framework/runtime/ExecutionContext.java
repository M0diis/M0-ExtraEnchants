package me.m0dii.extraenchants.framework.runtime;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.framework.model.CustomEnchantDefinition;
import me.m0dii.extraenchants.framework.model.TriggerDefinition;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExecutionContext {
    private final ExtraEnchants plugin;
    private final CustomEnchantDefinition enchant;
    private final TriggerDefinition trigger;
    private final TriggerType triggerType;
    private final Event event;
    private final LivingEntity attacker;
    private final LivingEntity victim;
    private final Player owner;
    private final int level;
    private final Location location;
    private final Map<String, Double> variables = new HashMap<>();
    private int chainDepth = 0;

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
        this.plugin = plugin;
        this.enchant = enchant;
        this.trigger = trigger;
        this.triggerType = triggerType;
        this.event = event;
        this.attacker = attacker;
        this.victim = victim;
        this.owner = owner;
        this.level = level;
        this.location = location;
    }

    public ExtraEnchants getPlugin() {
        return plugin;
    }

    public CustomEnchantDefinition getEnchant() {
        return enchant;
    }

    public TriggerDefinition getTrigger() {
        return trigger;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public Event getEvent() {
        return event;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }

    public LivingEntity getVictim() {
        return victim;
    }

    public Player getOwner() {
        return owner;
    }

    public int getLevel() {
        return level;
    }

    public Location getLocation() {
        return location;
    }

    public UUID getOwnerId() {
        return owner == null ? null : owner.getUniqueId();
    }

    public Map<String, Double> getVariables() {
        return variables;
    }

    public int getChainDepth() {
        return chainDepth;
    }

    public void incrementChainDepth() {
        this.chainDepth++;
    }
}

