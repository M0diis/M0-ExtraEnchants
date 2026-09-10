package me.m0dii.extraenchants.framework.runtime;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public class StatusStackService {
    private final Map<String, StackState> stacks = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> keysByPlayer = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    public StatusStackService() {
        this(System::currentTimeMillis);
    }

    public StatusStackService(LongSupplier clock) {
        this.clock = clock == null ? System::currentTimeMillis : clock;
    }

    public synchronized int get(String key) {
        if (key == null || key.isBlank()) {
            return 0;
        }
        cleanup(key);
        StackState state = stacks.get(key);
        return state == null ? 0 : state.value;
    }

    public synchronized int add(String key, int amount, long durationMillis) {
        if (key == null || key.isBlank()) {
            return 0;
        }

        cleanup(key);

        long until = expiresAt(durationMillis);
        StackState state = stacks.get(key);
        if (state == null) {
            state = new StackState(Math.max(0, amount), until);
            stacks.put(key, state);
            return state.value;
        }

        state.value = Math.max(0, state.value + amount);
        state.expiresAt = Math.max(state.expiresAt, until);
        return state.value;
    }

    public synchronized int get(UUID playerId, String key) {
        return playerId == null || key == null || key.isBlank() ? get(key) : get(scopedKey(playerId, key));
    }

    public synchronized int add(UUID playerId, String key, int amount, long durationMillis) {
        if (playerId == null || key == null || key.isBlank()) {
            return add(key, amount, durationMillis);
        }

        String stateKey = scopedKey(playerId, key);
        int value = add(stateKey, amount, durationMillis);
        if (key != null && !key.isBlank()) {
            keysByPlayer.computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet()).add(stateKey);
        }
        return value;
    }

    public synchronized void clear(UUID playerId, String key) {
        if (playerId == null || key == null || key.isBlank()) {
            clear(key);
            return;
        }
        clearState(playerId, scopedKey(playerId, key));
    }

    public synchronized void clear(String key) {
        if (key == null) {
            return;
        }

        stacks.remove(key);
        for (Map.Entry<UUID, Set<String>> entry : keysByPlayer.entrySet()) {
            entry.getValue().remove(key);
            if (entry.getValue().isEmpty()) {
                keysByPlayer.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    public synchronized void clearPlayer(UUID playerId) {
        if (playerId == null) {
            return;
        }

        Set<String> keys = keysByPlayer.remove(playerId);
        if (keys != null) {
            keys.forEach(stacks::remove);
        }
    }

    public synchronized void clear() {
        stacks.clear();
        keysByPlayer.clear();
    }

    public synchronized void cleanupExpired() {
        stacks.keySet().forEach(this::cleanup);
    }

    private void cleanup(String key) {
        StackState state = stacks.get(key);
        if (state != null && state.expiresAt <= clock.getAsLong()) {
            stacks.remove(key);
            for (Map.Entry<UUID, Set<String>> entry : keysByPlayer.entrySet()) {
                entry.getValue().remove(key);
                if (entry.getValue().isEmpty()) {
                    keysByPlayer.remove(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void clearState(UUID playerId, String stateKey) {
        stacks.remove(stateKey);
        Set<String> keys = keysByPlayer.get(playerId);
        if (keys != null) {
            keys.remove(stateKey);
            if (keys.isEmpty()) {
                keysByPlayer.remove(playerId, keys);
            }
        }
    }

    private String scopedKey(UUID playerId, String key) {
        return playerId + "\u0000" + key;
    }

    private long expiresAt(long durationMillis) {
        if (durationMillis <= 0) {
            return Long.MAX_VALUE;
        }

        long now = clock.getAsLong();
        return durationMillis >= Long.MAX_VALUE - now ? Long.MAX_VALUE : now + durationMillis;
    }

    private static final class StackState {
        private int value;
        private long expiresAt;

        private StackState(int value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }
}

