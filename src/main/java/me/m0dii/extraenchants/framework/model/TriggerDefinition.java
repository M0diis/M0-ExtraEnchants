package me.m0dii.extraenchants.framework.model;

import me.m0dii.extraenchants.framework.api.Condition;

import java.util.ArrayList;
import java.util.List;

public class TriggerDefinition {
    private final String name;
    private String chance = "100";
    private String cooldown = "0";
    private String globalCooldown = "0";
    private String target = "VICTIM";
    private String radius = "6";
    private int priority = 0;
    private int delayTicks = 0;
    private int repeatCount = 1;
    private int repeatIntervalTicks = 1;
    private boolean cancelEvent = false;
    private int activationLimit = -1;
    private final List<String> chainTriggers = new ArrayList<>();
    private final List<Condition> conditions = new ArrayList<>();
    private final List<EffectDefinition> effects = new ArrayList<>();

    public TriggerDefinition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getChance() {
        return chance;
    }

    public void setChance(String chance) {
        this.chance = chance;
    }

    public String getCooldown() {
        return cooldown;
    }

    public void setCooldown(String cooldown) {
        this.cooldown = cooldown;
    }

    public String getGlobalCooldown() {
        return globalCooldown;
    }

    public void setGlobalCooldown(String globalCooldown) {
        this.globalCooldown = globalCooldown;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getDelayTicks() {
        return delayTicks;
    }

    public void setDelayTicks(int delayTicks) {
        this.delayTicks = delayTicks;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = Math.max(1, repeatCount);
    }

    public int getRepeatIntervalTicks() {
        return repeatIntervalTicks;
    }

    public void setRepeatIntervalTicks(int repeatIntervalTicks) {
        this.repeatIntervalTicks = Math.max(1, repeatIntervalTicks);
    }

    public boolean isCancelEvent() {
        return cancelEvent;
    }

    public void setCancelEvent(boolean cancelEvent) {
        this.cancelEvent = cancelEvent;
    }

    public int getActivationLimit() {
        return activationLimit;
    }

    public void setActivationLimit(int activationLimit) {
        this.activationLimit = activationLimit;
    }

    public List<String> getChainTriggers() {
        return chainTriggers;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public List<EffectDefinition> getEffects() {
        return effects;
    }
}

