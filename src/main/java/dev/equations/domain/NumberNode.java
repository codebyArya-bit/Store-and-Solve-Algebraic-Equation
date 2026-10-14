package dev.equations.domain;

import java.util.Map;

public record NumberNode(double value, String literal) implements ExpressionNode {
    @Override public double evaluate(Map<String, Double> values) { return value; }
    @Override public String infix() { return literal; }
}
