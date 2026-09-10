package me.m0dii.extraenchants.framework.runtime;

import me.m0dii.extraenchants.framework.model.CustomEnchantDefinition;
import me.m0dii.extraenchants.framework.model.TriggerDefinition;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeSafetyTest {
    @Test
    void activationLimitIsScopedPerPlayerAndClears() {
        ActivationService service = new ActivationService();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        assertTrue(service.tryActivate(first, "venom:onattack", 1));
        assertTrue(!service.tryActivate(first, "venom:onattack", 1));
        assertTrue(service.tryActivate(second, "venom:onattack", 1));

        service.clearPlayer(first);
        assertTrue(service.tryActivate(first, "venom:onattack", 1));
    }

    @Test
    void cooldownsSupportIndependentPlayersAndGlobalScope() {
        AtomicLong now = new AtomicLong(1_000L);
        CooldownService service = new CooldownService(now::get);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        assertTrue(service.tryAcquire(first, "test", 500L, 0L));
        assertTrue(!service.tryAcquire(first, "test", 500L, 0L));
        assertTrue(service.tryAcquire(second, "test", 500L, 500L));
        now.addAndGet(501L);
        assertTrue(service.tryAcquire(first, "test", 500L, 0L));
    }

    @Test
    void cooldownRollbackCannotClearANewerReservation() {
        AtomicLong now = new AtomicLong(1_000L);
        CooldownService service = new CooldownService(now::get);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        CooldownService.Reservation oldReservation = service.tryReserve(first, "shared", 0L, 100L);
        assertNotNull(oldReservation);
        now.addAndGet(101L);
        assertNotNull(service.tryReserve(second, "shared", 0L, 100L));

        service.release(oldReservation);

        assertTrue(service.isOnGlobalCooldown("shared"));
    }

    @Test
    void chainContextPropagatesDepthAndRejectsCycles() {
        CustomEnchantDefinition definition = new CustomEnchantDefinition("chain");
        TriggerDefinition rootTrigger = new TriggerDefinition("onAttack");
        TriggerDefinition childTrigger = new TriggerDefinition("onKill");
        definition.getTriggers().put(rootTrigger.getName(), rootTrigger);
        definition.getTriggers().put(childTrigger.getName(), childTrigger);

        ExecutionContext root = new ExecutionContext(
                null, definition, rootTrigger, TriggerType.ON_ATTACK,
                null, null, null, null, 1, null);
        ExecutionContext child = root.createChildContext(definition, childTrigger, TriggerType.ON_KILL);

        assertNotNull(child);
        assertEquals(1, child.getChainDepth());
        assertEquals(2, child.getChainPath().size());
        assertNull(child.createChildContext(definition, rootTrigger, TriggerType.ON_ATTACK));
    }

    @Test
    void playerStateCleanupRemovesAllIndexedState() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        StatusStackService stacks = new StatusStackService(() -> 100L);
        ComboStateService combos = new ComboStateService(() -> 100L);

        stacks.add(first, "first", 2, 10_000L);
        stacks.add(second, "second", 3, 10_000L);
        combos.registerHit(first, "first-combo", 10_000L);
        combos.registerHit(second, "second-combo", 10_000L);

        stacks.clearPlayer(first);
        combos.clearPlayer(first);

        assertEquals(0, stacks.get(first, "first"));
        assertEquals(3, stacks.get(second, "second"));
        assertEquals(0, combos.getHits(first, "first-combo", 10_000L));
        assertEquals(1, combos.getHits(second, "second-combo", 10_000L));
    }

    @Test
    void sameRawStateKeyIsStillScopedPerPlayer() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        StatusStackService stacks = new StatusStackService(() -> 100L);
        ComboStateService combos = new ComboStateService(() -> 100L);

        stacks.add(first, "shared", 2, 10_000L);
        stacks.add(second, "shared", 3, 10_000L);
        combos.registerHit(first, "shared", 10_000L);
        combos.registerHit(second, "shared", 10_000L);

        stacks.clearPlayer(first);
        combos.clearPlayer(first);

        assertEquals(0, stacks.get(first, "shared"));
        assertEquals(3, stacks.get(second, "shared"));
        assertEquals(0, combos.getHits(first, "shared", 10_000L));
        assertEquals(1, combos.getHits(second, "shared", 10_000L));
    }
}
