package me.m0dii.extraenchants.framework.api;

import java.util.Map;

public interface FormulaEngine {
    double evaluate(String expression, Map<String, Double> variables);

    default int evaluateInt(String expression, Map<String, Double> variables) {
        return (int) Math.round(evaluate(expression, variables));
    }
}

