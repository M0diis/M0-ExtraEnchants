package me.m0dii.extraenchants.framework.runtime;

import java.util.HashMap;
import java.util.Map;

public class StatusStackService {
    private final Map<String, StackState> stacks = new HashMap<>();

    public int get(String key) {
        cleanup(key);
        StackState state = stacks.get(key);
        return state == null ? 0 : state.value;
    }

    public int add(String key, int amount, long durationMillis) {
        cleanup(key);

        long until = durationMillis <= 0 ? Long.MAX_VALUE : System.currentTimeMillis() + durationMillis;
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

    public void clear(String key) {
        stacks.remove(key);
    }

    private void cleanup(String key) {
        StackState state = stacks.get(key);
        if (state != null && state.expiresAt < System.currentTimeMillis()) {
            stacks.remove(key);
        }
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

