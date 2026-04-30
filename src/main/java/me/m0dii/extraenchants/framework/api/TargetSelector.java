package me.m0dii.extraenchants.framework.api;

import me.m0dii.extraenchants.framework.runtime.ExecutionContext;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;

@FunctionalInterface
public interface TargetSelector {
    Collection<LivingEntity> select(ExecutionContext context, Object config);
}

