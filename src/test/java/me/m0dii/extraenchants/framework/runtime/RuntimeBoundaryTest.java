package me.m0dii.extraenchants.framework.runtime;

import me.m0dii.extraenchants.framework.api.Condition;
import me.m0dii.extraenchants.framework.config.ConditionParser;
import me.m0dii.extraenchants.framework.registry.ConditionRegistry;
import me.m0dii.extraenchants.framework.registry.TargetRegistry;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeBoundaryTest {
    @Test
    void conditionParserEvaluatesComparisons() {
        ConditionParser parser = new ConditionParser(new ConditionRegistry(), new Exp4jFormulaEngine());
        Condition condition = parser.parse("level >= 2");
        ExecutionContext context = new ExecutionContext(
                null, null, null, TriggerType.CUSTOM, null, null, null, null, 3, null);

        assertTrue(condition.test(context, null));
    }

    @Test
    void targetingServiceResolvesSelfWithoutGlobalBukkitScheduler() {
        TargetingService service = new TargetingService(new TargetRegistry(), new Exp4jFormulaEngine());
        Player owner = Mockito.mock(Player.class);
        ExecutionContext context = new ExecutionContext(
                null, null, null, TriggerType.CUSTOM, null, null, null, owner, 1, null);

        assertEquals(List.of(owner), List.copyOf(service.resolve("SELF", context, "6")));
    }
}
