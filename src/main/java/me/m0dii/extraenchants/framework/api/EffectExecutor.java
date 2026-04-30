package me.m0dii.extraenchants.framework.api;

import me.m0dii.extraenchants.framework.runtime.ExecutionContext;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;

@FunctionalInterface
public interface EffectExecutor {
    void execute(ExecutionContext context, Collection<LivingEntity> targets, Object value);
}

