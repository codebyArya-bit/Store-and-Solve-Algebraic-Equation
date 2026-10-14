package dev.equations.domain;

import java.util.Map;

public record OperatorNode(String operator, ExpressionNode left, ExpressionNode right) implements ExpressionNode {
    @Override public double evaluate(Map<String, Double> values) {
        double b = right.evaluate(values);
        double a = left == null ? 0 : left.evaluate(values);
        double result = switch (operator) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> {
                if (b == 0) throw new EquationException("Division by zero");
                yield a / b;
            }
            case "^" -> Math.pow(a, b);
            case "u-" -> -b;
            case "u+" -> b;
            default -> throw new EquationException("Unknown operator");
        };
        if (!Double.isFinite(result)) throw new EquationException("Expression produces a non-finite result");
        return result;
    }
    @Override public String infix() {
        return left == null
            ? "(" + (operator.equals("u-") ? "-" : "+") + right.infix() + ")"
            : "(" + left.infix() + " " + operator + " " + right.infix() + ")";
    }
}
