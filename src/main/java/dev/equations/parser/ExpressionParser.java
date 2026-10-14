package dev.equations.parser;

import dev.equations.domain.*;
import java.util.*;

/** Tokenize -> insert implicit multiplication -> shunting-yard postfix -> expression tree. */
public final class ExpressionParser {
    private enum Kind { NUMBER, VARIABLE, OP, LEFT, RIGHT }
    private record Token(Kind kind, String value) {}

    public ExpressionNode parse(String input) {
        if (input == null || input.isBlank()) throw new EquationException("Equation must not be blank");
        if (input.length() > 2000) throw new EquationException("Equation exceeds 2000 characters");
        return buildTree(toPostfix(tokenize(input)));
    }

    private List<Token> tokenize(String text) {
        List<Token> result = new ArrayList<>();
        for (int i = 0; i < text.length();) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) { i++; continue; }
            int start = i;
            Token token;
            if (Character.isDigit(c) || c == '.') {
                i++;
                while (i < text.length() && (Character.isDigit(text.charAt(i)) || text.charAt(i) == '.')) i++;
                String literal = text.substring(start, i);
                try {
                    if (!Double.isFinite(Double.parseDouble(literal))) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    throw new EquationException("Invalid number: " + literal);
                }
                token = new Token(Kind.NUMBER, literal);
            } else if (Character.isLetter(c)) {
                // One-character variables: 3xy means 3 * x * y.
                i++;
                token = new Token(Kind.VARIABLE, String.valueOf(c));
            } else {
                i++;
                token = switch (c) {
                    case '+', '-', '*', '/', '^' -> new Token(Kind.OP, String.valueOf(c));
                    case '(' -> new Token(Kind.LEFT, "(");
                    case ')' -> new Token(Kind.RIGHT, ")");
                    default -> throw new EquationException("Invalid character at position " + start + ": " + c);
                };
            }
            if (!result.isEmpty()) {
                Kind prev = result.get(result.size() - 1).kind();
                if ((prev == Kind.NUMBER || prev == Kind.VARIABLE || prev == Kind.RIGHT)
                        && (token.kind() == Kind.VARIABLE || token.kind() == Kind.LEFT
                        || (token.kind() == Kind.NUMBER && prev == Kind.RIGHT))) {
                    result.add(new Token(Kind.OP, "*"));
                }
            }
            result.add(token);
        }
        return result;
    }

    private static int precedence(String op) {
        return switch (op) { case "+", "-" -> 1; case "*", "/" -> 2;
            case "u+", "u-" -> 3; case "^" -> 4; default -> -1; };
    }
    private static boolean rightAssociative(String op) {
        return op.equals("^") || op.startsWith("u");
    }

    private List<Token> toPostfix(List<Token> tokens) {
        List<Token> output = new ArrayList<>();
        Deque<Token> operators = new ArrayDeque<>();
        boolean expectOperand = true;
        for (Token token : tokens) {
            switch (token.kind()) {
                case NUMBER, VARIABLE -> {
                    if (!expectOperand) throw new EquationException("Missing operator");
                    output.add(token); expectOperand = false;
                }
                case LEFT -> {
                    if (!expectOperand) throw new EquationException("Missing operator before '('");
                    operators.push(token); expectOperand = true;
                }
                case RIGHT -> {
                    if (expectOperand) throw new EquationException("Empty or incomplete parentheses");
                    while (!operators.isEmpty() && operators.peek().kind() != Kind.LEFT)
                        output.add(operators.pop());
                    if (operators.isEmpty()) throw new EquationException("Unmatched closing parenthesis");
                    operators.pop(); expectOperand = false;
                }
                case OP -> {
                    String op = token.value();
                    if (expectOperand) {
                        if (!op.equals("+") && !op.equals("-"))
                            throw new EquationException("Expected operand before '" + op + "'");
                        operators.push(new Token(Kind.OP, "u" + op));
                        continue;
                    }
                    while (!operators.isEmpty() && operators.peek().kind() == Kind.OP) {
                        String prior = operators.peek().value();
                        if (precedence(prior) > precedence(op)
                                || (!rightAssociative(op) && precedence(prior) == precedence(op)))
                            output.add(operators.pop());
                        else break;
                    }
                    operators.push(token); expectOperand = true;
                }
            }
        }
        if (expectOperand) throw new EquationException("Equation ends with an operator or is empty");
        while (!operators.isEmpty()) {
            Token token = operators.pop();
            if (token.kind() == Kind.LEFT) throw new EquationException("Unmatched opening parenthesis");
            output.add(token);
        }
        return output;
    }

    private ExpressionNode buildTree(List<Token> postfix) {
        Deque<ExpressionNode> stack = new ArrayDeque<>();
        for (Token token : postfix) {
            if (token.kind() == Kind.NUMBER)
                stack.push(new NumberNode(Double.parseDouble(token.value()), token.value()));
            else if (token.kind() == Kind.VARIABLE)
                stack.push(new VariableNode(token.value()));
            else {
                if (stack.isEmpty()) throw new EquationException("Missing operand");
                ExpressionNode right = stack.pop();
                ExpressionNode left = null;
                if (!token.value().startsWith("u")) {
                    if (stack.isEmpty()) throw new EquationException("Missing left operand");
                    left = stack.pop();
                }
                stack.push(new OperatorNode(token.value(), left, right));
            }
        }
        if (stack.size() != 1) throw new EquationException("Invalid expression");
        return stack.pop();
    }
}
