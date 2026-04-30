package me.m0dii.extraenchants.framework.runtime;

import java.util.HashMap;
import java.util.Map;

public class ComboStateService {
    private final Map<String, ComboState> combos = new HashMap<>();

    public int registerHit(String key, long windowMillis) {
        long now = System.currentTimeMillis();
        ComboState state = combos.get(key);

        if (state == null || now - state.lastHit > windowMillis) {
            combos.put(key, new ComboState(1, now));
            return 1;
        }

        state.hits++;
        state.lastHit = now;
        return state.hits;
    }

    public int getHits(String key, long windowMillis) {
        ComboState state = combos.get(key);
        if (state == null) {
            return 0;
        }

        long now = System.currentTimeMillis();
        if (now - state.lastHit > windowMillis) {
            combos.remove(key);
            return 0;
        }

        return state.hits;
    }

    public void reset(String key) {
        combos.remove(key);
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

