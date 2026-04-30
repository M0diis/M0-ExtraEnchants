package me.m0dii.extraenchants.framework;

import me.m0dii.extraenchants.framework.runtime.ComboStateService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComboStateServiceTest {
    @Test
    void incrementsAndExpiresCombo() throws InterruptedException {
        ComboStateService service = new ComboStateService();
        String key = "venom:test:test";

        assertEquals(1, service.registerHit(key, 100));
        assertEquals(2, service.registerHit(key, 100));
        Thread.sleep(120);
        assertEquals(0, service.getHits(key, 100));
        assertEquals(1, service.registerHit(key, 100));
    }
}

