package me.m0dii.extraenchants.framework.runtime;

import me.m0dii.extraenchants.framework.api.FormulaEngine;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Exp4jFormulaEngine implements FormulaEngine {
    @Override
    public double evaluate(String expression, Map<String, Double> variables) {
        if (expression == null || expression.isBlank()) {
            return 0D;
        }

        String candidate = expression.trim();

        try {
            Set<String> keys = variables == null ? Set.of() : variables.keySet();
            Expression exp = new ExpressionBuilder(candidate)
                    .variables(keys)
                    .build();

            if (variables != null && !variables.isEmpty()) {
                exp.setVariables(new HashMap<>(variables));
            }

            return exp.evaluate();
        } catch (Exception ex) {
            return parseFallback(candidate);
        }
    }

    private double parseFallback(String expression) {
        try {
            return Double.parseDouble(expression);
        } catch (NumberFormatException ignored) {
            return 0D;
        }
    }
}

