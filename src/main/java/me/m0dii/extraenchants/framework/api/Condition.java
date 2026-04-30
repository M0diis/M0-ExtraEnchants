package me.m0dii.extraenchants.framework.api;

import me.m0dii.extraenchants.framework.runtime.ExecutionContext;
import org.bukkit.entity.LivingEntity;

@FunctionalInterface
public interface Condition {
    boolean test(ExecutionContext context, LivingEntity target);
}

