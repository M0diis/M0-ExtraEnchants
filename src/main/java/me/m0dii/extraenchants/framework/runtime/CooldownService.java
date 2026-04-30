package me.m0dii.extraenchants.framework.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownService {
    private final Map<String, Long> globalCooldowns = new HashMap<>();
    private final Map<UUID, Map<String, Long>> playerCooldowns = new HashMap<>();

    public boolean isOnGlobalCooldown(String key) {
        Long until = globalCooldowns.get(key);
        return until != null && until > System.currentTimeMillis();
    }

    public void putGlobalCooldown(String key, long millis) {
        if (millis <= 0) {
            return;
        }

        globalCooldowns.put(key, System.currentTimeMillis() + millis);
    }

    public boolean isOnPlayerCooldown(UUID playerId, String key) {
        if (playerId == null) {
            return false;
        }

        Map<String, Long> map = playerCooldowns.get(playerId);
        if (map == null) {
            return false;
        }

        Long until = map.get(key);
        return until != null && until > System.currentTimeMillis();
    }

    public void putPlayerCooldown(UUID playerId, String key, long millis) {
        if (playerId == null || millis <= 0) {
            return;
        }

        playerCooldowns
                .computeIfAbsent(playerId, ignored -> new HashMap<>())
                .put(key, System.currentTimeMillis() + millis);
    }

    public void clear() {
        globalCooldowns.clear();
        playerCooldowns.clear();
    }
}

