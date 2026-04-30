package me.m0dii.extraenchants.framework;

import me.m0dii.extraenchants.framework.runtime.StatusStackService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusStackServiceTest {
    @Test
    void handlesStackingAndExpiry() throws InterruptedException {
        StatusStackService service = new StatusStackService();
        String key = "venom:player";

        assertEquals(2, service.add(key, 2, 120));
        assertEquals(3, service.add(key, 1, 120));
        assertEquals(3, service.get(key));

        Thread.sleep(150);
        assertEquals(0, service.get(key));
    }
}

