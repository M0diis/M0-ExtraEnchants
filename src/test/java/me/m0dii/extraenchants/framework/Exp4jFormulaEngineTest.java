package me.m0dii.extraenchants.framework;

import me.m0dii.extraenchants.framework.runtime.Exp4jFormulaEngine;
import me.m0dii.extraenchants.framework.runtime.TriggerType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class Exp4jFormulaEngineTest {
    private final Exp4jFormulaEngine formulaEngine = new Exp4jFormulaEngine();

    @Test
    void evaluatesExpressionWithVariables() {
        double value = formulaEngine.evaluate("15 + level * 5", Map.of("level", 3D));
        assertEquals(30D, value, 0.001D);
    }

    @Test
    void invalidExpressionFallsBackToZero() {
        double value = formulaEngine.evaluate("level +", Map.of("level", 3D));
        assertEquals(0D, value, 0.001D);
    }

    @Test
    void parsesTriggerKeys() {
        assertEquals(TriggerType.ON_ATTACK, TriggerType.fromKey("onAttack"));
        assertEquals(TriggerType.ON_BLOCK_BREAK, TriggerType.fromKey("block-break"));
        assertNull(TriggerType.fromKey("nonExisting"));
    }
}

