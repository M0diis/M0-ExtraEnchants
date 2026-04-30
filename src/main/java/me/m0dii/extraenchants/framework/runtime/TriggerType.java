package me.m0dii.extraenchants.framework.runtime;

import java.util.Arrays;

public enum TriggerType {
    ON_ATTACK,
    ON_DAMAGED,
    ON_KILL,
    ON_DEATH,
    ON_SHOOT,
    ON_PROJECTILE_HIT,
    ON_BLOCK_BREAK,
    ON_MINE,
    ON_MOVE,
    ON_JUMP,
    ON_SNEAK,
    ON_SPRINT,
    ON_EQUIP,
    ON_UNEQUIP,
    ON_CONSUME,
    ON_FISH,
    ON_INTERACT,
    ON_RIGHT_CLICK,
    ON_LEFT_CLICK,
    ON_FALL,
    ON_CHAT,
    CUSTOM;

    public static TriggerType fromKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        String normalized = key.trim()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase();

        if (!normalized.startsWith("ON_") && !normalized.equals("CUSTOM")) {
            normalized = "ON_" + normalized;
        }

        final String lookup = normalized;

        return Arrays.stream(values())
                .filter(v -> v.name().equals(lookup))
                .findFirst()
                .orElse(null);
    }
}

