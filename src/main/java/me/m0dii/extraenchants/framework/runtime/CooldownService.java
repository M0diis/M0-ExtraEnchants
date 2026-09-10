package me.m0dii.extraenchants.framework.runtime;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public class CooldownService {
    private final Map<String, Long> globalCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>> playerCooldowns = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    public CooldownService() {
        this(System::currentTimeMillis);
    }

    public CooldownService(LongSupplier clock) {
        this.clock = clock == null ? System::currentTimeMillis : clock;
    }

    public synchronized boolean isOnGlobalCooldown(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }

        Long until = globalCooldowns.get(key);
        if (until == null) {
            return false;
        }

        if (until <= clock.getAsLong()) {
            globalCooldowns.remove(key, until);
            return false;
        }

        return true;
    }

    public synchronized void putGlobalCooldown(String key, long millis) {
        if (key == null || key.isBlank() || millis <= 0) {
            return;
        }

        globalCooldowns.put(key, expiresAt(millis));
    }

    public synchronized boolean isOnPlayerCooldown(UUID playerId, String key) {
        if (playerId == null || key == null || key.isBlank()) {
            return false;
        }

        Map<String, Long> map = playerCooldowns.get(playerId);
        if (map == null) {
            return false;
        }

        Long until = map.get(key);
        if (until == null) {
            return false;
        }

        if (until <= clock.getAsLong()) {
            map.remove(key, until);
            if (map.isEmpty()) {
                playerCooldowns.remove(playerId, map);
            }
            return false;
        }

        return true;
    }

    public synchronized void putPlayerCooldown(UUID playerId, String key, long millis) {
        if (playerId == null || key == null || key.isBlank() || millis <= 0) {
            return;
        }

        playerCooldowns
                .computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
                .put(key, expiresAt(millis));
    }

    /**
     * Atomically checks and records both cooldown scopes. The global map is
     * intentionally shared by every player; only the player map is scoped to
     * {@code playerId}.
     */
    public synchronized boolean tryAcquire(
            UUID playerId,
            String key,
            long playerMillis,
            long globalMillis
    ) {
        return tryReserve(playerId, key, playerMillis, globalMillis) != null;
    }

    /**
     * Atomically claims both cooldown scopes and returns the exact expiry
     * values owned by the caller. Those values make a later rollback safe even
     * if another activation has already claimed the same key.
     */
    public synchronized Reservation tryReserve(
            UUID playerId,
            String key,
            long playerMillis,
            long globalMillis
    ) {
        if (key == null || key.isBlank()) {
            return null;
        }

        if (isOnGlobalCooldown(key) || isOnPlayerCooldown(playerId, key)) {
            return null;
        }

        long now = clock.getAsLong();
        long playerUntil = playerMillis > 0 ? expiresAt(now, playerMillis) : 0L;
        long globalUntil = globalMillis > 0 ? expiresAt(now, globalMillis) : 0L;

        if (playerUntil > 0 && playerId != null) {
            playerCooldowns
                    .computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
                    .put(key, playerUntil);
        }
        if (globalUntil > 0) {
            globalCooldowns.put(key, globalUntil);
        }

        return new Reservation(playerId, key, playerUntil, globalUntil);
    }

    /** Releases a reservation made by {@link #tryReserve} when a later gate fails. */
    public synchronized void release(Reservation reservation) {
        if (reservation == null || reservation.key() == null || reservation.key().isBlank()) {
            return;
        }

        if (reservation.globalUntil() > 0) {
            globalCooldowns.remove(reservation.key(), reservation.globalUntil());
        }

        if (reservation.playerId() == null || reservation.playerUntil() <= 0) {
            return;
        }

        Map<String, Long> playerMap = playerCooldowns.get(reservation.playerId());
        if (playerMap != null) {
            playerMap.remove(reservation.key(), reservation.playerUntil());
            if (playerMap.isEmpty()) {
                playerCooldowns.remove(reservation.playerId(), playerMap);
            }
        }
    }

    /**
     * Backwards-compatible unconditional release. New dispatch code should
     * release the reservation token instead.
     */
    public synchronized void release(UUID playerId, String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        globalCooldowns.remove(key);
        if (playerId == null) {
            return;
        }

        Map<String, Long> playerMap = playerCooldowns.get(playerId);
        if (playerMap != null) {
            playerMap.remove(key);
            if (playerMap.isEmpty()) {
                playerCooldowns.remove(playerId, playerMap);
            }
        }
    }

    public synchronized void clearPlayer(UUID playerId) {
        if (playerId != null) {
            playerCooldowns.remove(playerId);
        }
    }

    public synchronized int playerEntryCount() {
        return playerCooldowns.size();
    }

    public synchronized int globalEntryCount() {
        return globalCooldowns.size();
    }

    public synchronized void clear() {
        globalCooldowns.clear();
        playerCooldowns.clear();
    }

    private long expiresAt(long millis) {
        return expiresAt(clock.getAsLong(), millis);
    }

    private long expiresAt(long now, long millis) {
        if (millis >= Long.MAX_VALUE - now) {
            return Long.MAX_VALUE;
        }

        return now + millis;
    }

    public record Reservation(UUID playerId, String key, long playerUntil, long globalUntil) {
    }
}

