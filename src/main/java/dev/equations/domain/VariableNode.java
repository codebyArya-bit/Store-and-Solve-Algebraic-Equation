package dev.equations.domain;

import java.util.Map;

public record VariableNode(String name) implements ExpressionNode {
    @Override public double evaluate(Map<String, Double> values) {
        if (values == null || !values.containsKey(name) || values.get(name) == null
                || !Double.isFinite(values.get(name))) {
            throw new EquationException("Missing or invalid variable: " + name);
        }
        return values.get(name);
    }
    @Override public String infix() { return name; }
}
