package me.m0dii.extraenchants.framework.runtime;

import me.m0dii.extraenchants.framework.api.FormulaEngine;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Exp4jFormulaEngine implements FormulaEngine {
    private static final int CACHE_LIMIT = 512;
    private final Map<String, Expression> cache = Collections.synchronizedMap(new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Expression> eldest) {
            return size() > CACHE_LIMIT;
        }
    });

    @Override
    public double evaluate(String expression, Map<String, Double> variables) {
        if (expression == null || expression.isBlank()) {
            return 0D;
        }

        String candidate = expression.trim();

        try {
            Set<String> keys = variables == null ? Set.of() : variables.keySet();
            String cacheKey = cacheKey(candidate, keys);
            Expression exp;
            synchronized (cache) {
                exp = cache.get(cacheKey);
                if (exp == null) {
                    exp = new ExpressionBuilder(candidate)
                            .variables(keys)
                            .build();
                    cache.put(cacheKey, exp);
                }
            }

            synchronized (exp) {
                if (variables != null && !variables.isEmpty()) {
                    exp.setVariables(new HashMap<>(variables));
                }
                return exp.evaluate();
            }
        } catch (Exception ex) {
            return parseFallback(candidate);
        }
    }

    private String cacheKey(String expression, Set<String> variableKeys) {
        if (variableKeys == null || variableKeys.isEmpty()) {
            return expression;
        }

        List<String> sorted = new ArrayList<>(variableKeys);
        Collections.sort(sorted);
        return expression + "||" + String.join(",", sorted);
    }

    private double parseFallback(String expression) {
        try {
            return Double.parseDouble(expression);
        } catch (NumberFormatException ignored) {
            return 0D;
        }
    }
}

