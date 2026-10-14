package dev.equations.domain;

import java.util.Map;

/** Immutable expression-tree node (Composite pattern). */
public sealed interface ExpressionNode permits NumberNode, VariableNode, OperatorNode {
    double evaluate(Map<String, Double> values);
    String infix();
}
