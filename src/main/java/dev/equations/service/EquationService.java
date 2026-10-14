package dev.equations.service;

import dev.equations.domain.*;
import dev.equations.parser.ExpressionParser;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Application service with deterministic IDs and thread-safe, in-memory state. */
@Service
public class EquationService {
    private final ExpressionParser parser = new ExpressionParser();
    private final AtomicLong nextId = new AtomicLong();
    private final Map<String, StoredEquation> equations = new ConcurrentHashMap<>();

    public record StoredEquation(String id, String original, ExpressionNode tree) {}

    public String store(String equation) {
        ExpressionNode tree = parser.parse(equation);
        String id = String.valueOf(nextId.incrementAndGet());
        equations.put(id, new StoredEquation(id, equation, tree));
        return id;
    }

    public List<Map<String, String>> list() {
        return equations.values().stream()
            .sorted(Comparator.comparingLong(item -> Long.parseLong(item.id())))
            .map(item -> Map.of("equationId", item.id(), "equation", item.tree().infix()))
            .toList();
    }

    public Map<String, Object> evaluate(String id, Map<String, Double> values) {
        StoredEquation stored = equations.get(id);
        if (stored == null) throw new NoSuchElementException("Equation not found: " + id);
        if (values == null) throw new EquationException("variables must be provided");
        double result = stored.tree().evaluate(values);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("equationId", id);
        response.put("equation", stored.original());
        response.put("variables", values);
        response.put("result", result);
        return response;
    }
}
