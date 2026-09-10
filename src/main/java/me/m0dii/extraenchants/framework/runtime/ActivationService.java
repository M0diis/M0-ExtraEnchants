package me.m0dii.extraenchants.framework.runtime;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks activation limits independently for every player.
 *
 * Activation limits are deliberately not folded into {@link CooldownService}:
 * a limit is a lifetime/session counter, while cooldowns are time windows.
 */
public final class ActivationService {
    private final Map<UUID, Map<String, Integer>> counts = new ConcurrentHashMap<>();

    public synchronized boolean tryActivate(UUID playerId, String key, int limit) {
        if (limit < 0) {
            return true;
        }

        if (playerId == null || key == null || key.isBlank() || limit == 0) {
            return false;
        }

        Map<String, Integer> playerCounts = counts.computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>());
        int current = playerCounts.getOrDefault(key, 0);
        if (current >= limit) {
            return false;
        }

        playerCounts.put(key, current + 1);
        return true;
    }

    public synchronized int get(UUID playerId, String key) {
        if (playerId == null || key == null) {
            return 0;
        }

        Map<String, Integer> playerCounts = counts.get(playerId);
        return playerCounts == null ? 0 : playerCounts.getOrDefault(key, 0);
    }

    public synchronized void clearPlayer(UUID playerId) {
        if (playerId != null) {
            counts.remove(playerId);
        }
    }

    public synchronized void clear() {
        counts.clear();
    }

    public int playerEntryCount() {
        return counts.size();
    }
}
