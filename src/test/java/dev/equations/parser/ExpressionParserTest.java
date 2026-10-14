package dev.equations.parser;

import dev.equations.domain.EquationException;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ExpressionParserTest {
    private final ExpressionParser parser = new ExpressionParser();

    @Test void implicitMultiplication() {
        assertEquals(11, parser.parse("3x + 2y - z").evaluate(Map.of("x", 2.0, "y", 3.0, "z", 1.0)));
        assertEquals(18, parser.parse("3xy").evaluate(Map.of("x", 2.0, "y", 3.0)));
    }
    @Test void exponentiation() {
        assertEquals(9, parser.parse("x^2 + y^2 - 4").evaluate(Map.of("x", 2.0, "y", 3.0)));
    }
    @Test void parentheses() {
        assertEquals(20, parser.parse("2(x+3)").evaluate(Map.of("x", 7.0)));
        assertEquals(10, parser.parse("(x+3)2").evaluate(Map.of("x", 2.0)));
    }
    @Test void precedenceAndAssociativity() {
        assertEquals(512, parser.parse("2^3^2").evaluate(Map.of()));
        assertEquals(-4, parser.parse("-2^2").evaluate(Map.of()));
        assertEquals(0.25, parser.parse("2^-2").evaluate(Map.of()));
    }
    @Test void decimalsAndUnarySigns() {
        assertEquals(-2.5, parser.parse("-0.5x").evaluate(Map.of("x", 5.0)));
        assertEquals(4, parser.parse("2--2").evaluate(Map.of()));
    }
    @Test void invalidSyntax() {
        for (String expr : new String[]{"", "x+", "(x+2", "x*", "1..2", "()", "x/", "x$2"}) {
            assertThrows(EquationException.class, () -> parser.parse(expr), expr);
        }
    }
    @Test void missingVariable() {
        assertThrows(EquationException.class, () -> parser.parse("x+1").evaluate(Map.of()));
    }
    @Test void divisionByZero() {
        assertThrows(EquationException.class, () -> parser.parse("x/(y-y)").evaluate(Map.of("x",1.0,"y",2.0)));
    }
    @Test void nonFiniteResult() {
        assertThrows(EquationException.class, () -> parser.parse("1000^1000").evaluate(Map.of()));
    }
    @Test void rejectsHugeInput() {
        assertThrows(EquationException.class, () -> parser.parse("x".repeat(2001)));
    }
}
