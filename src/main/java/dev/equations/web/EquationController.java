package dev.equations.web;

import dev.equations.service.EquationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/equations")
public class EquationController {
    private final EquationService service;
    public EquationController(EquationService service) { this.service = service; }

    public record StoreRequest(@NotBlank String equation) {}
    public record EvaluateRequest(@NotNull Map<String, Double> variables) {}

    @PostMapping("/store")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> store(@Valid @RequestBody StoreRequest request) {
        return Map.of("message", "Equation stored successfully", "equationId", service.store(request.equation()));
    }

    @GetMapping
    public Map<String, Object> list() {
        return Map.of("equations", service.list());
    }

    @PostMapping("/{equationId}/evaluate")
    public Map<String, Object> evaluate(@PathVariable String equationId, @Valid @RequestBody EvaluateRequest request) {
        return service.evaluate(equationId, request.variables());
    }
}
