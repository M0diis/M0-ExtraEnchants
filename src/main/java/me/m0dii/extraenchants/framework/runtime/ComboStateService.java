package me.m0dii.extraenchants.framework.runtime;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public class ComboStateService {
    private final Map<String, ComboState> combos = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> keysByPlayer = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    public ComboStateService() {
        this(System::currentTimeMillis);
    }

    public ComboStateService(LongSupplier clock) {
        this.clock = clock == null ? System::currentTimeMillis : clock;
    }

    public synchronized int registerHit(String key, long windowMillis) {
        if (key == null || key.isBlank()) {
            return 0;
        }

        long now = clock.getAsLong();
        ComboState state = combos.get(key);

        if (state == null || now - state.lastHit > windowMillis) {
            combos.put(key, new ComboState(1, now));
            return 1;
        }

        state.hits++;
        state.lastHit = now;
        return state.hits;
    }

    public synchronized int registerHit(UUID playerId, String key, long windowMillis) {
        if (playerId == null || key == null || key.isBlank()) {
            return registerHit(key, windowMillis);
        }

        String stateKey = scopedKey(playerId, key);
        int hits = registerHit(stateKey, windowMillis);
        if (key != null && !key.isBlank()) {
            keysByPlayer.computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet()).add(stateKey);
        }
        return hits;
    }

    public synchronized int getHits(String key, long windowMillis) {
        if (key == null || key.isBlank()) {
            return 0;
        }

        ComboState state = combos.get(key);
        if (state == null) {
            return 0;
        }

        long now = clock.getAsLong();
        if (now - state.lastHit > windowMillis) {
            remove(key);
            return 0;
        }

        return state.hits;
    }

    public synchronized int getHits(UUID playerId, String key, long windowMillis) {
        return playerId == null || key == null || key.isBlank()
                ? getHits(key, windowMillis) : getHits(scopedKey(playerId, key), windowMillis);
    }

    public synchronized void reset(String key) {
        remove(key);
    }

    public synchronized void reset(UUID playerId, String key) {
        if (playerId == null || key == null || key.isBlank()) {
            reset(key);
            return;
        }
        String stateKey = scopedKey(playerId, key);
        remove(stateKey);
    }

    public synchronized void clearPlayer(UUID playerId) {
        if (playerId == null) {
            return;
        }

        Set<String> keys = keysByPlayer.remove(playerId);
        if (keys != null) {
            keys.forEach(combos::remove);
        }
    }

    public synchronized void clear() {
        combos.clear();
        keysByPlayer.clear();
    }

    public synchronized void cleanupExpired(long windowMillis) {
        combos.keySet().stream()
                .filter(key -> {
                    ComboState state = combos.get(key);
                    return state != null && clock.getAsLong() - state.lastHit > windowMillis;
                })
                .toList()
                .forEach(this::remove);
    }

    private void remove(String key) {
        combos.remove(key);
        for (Map.Entry<UUID, Set<String>> entry : keysByPlayer.entrySet()) {
            entry.getValue().remove(key);
            if (entry.getValue().isEmpty()) {
                keysByPlayer.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    private String scopedKey(UUID playerId, String key) {
        return playerId + "\u0000" + key;
    }

    private static final class ComboState {
        private int hits;
        private long lastHit;

        private ComboState(int hits, long lastHit) {
            this.hits = hits;
            this.lastHit = lastHit;
        }
    }
}

