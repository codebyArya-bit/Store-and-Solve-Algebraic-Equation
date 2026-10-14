package dev.equations.service;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class EquationServiceTest {
    @Test void storesListsAndEvaluates() {
        EquationService service = new EquationService();
        String id = service.store("3x+2y-z");
        assertEquals("1", id);
        assertEquals(1, service.list().size());
        assertEquals(11.0, service.evaluate(id, Map.of("x",2.0,"y",3.0,"z",1.0)).get("result"));
    }
    @Test void rejectsUnknownId() {
        assertThrows(NoSuchElementException.class, () -> new EquationService().evaluate("404", Map.of()));
    }
    @Test void storesDistinctIdsAndCorrectOrdering() {
        EquationService service = new EquationService();
        for(int i=0; i<12; i++) service.store("x+" + i);
        assertEquals("1", service.list().get(0).get("equationId"));
        assertEquals("12", service.list().get(11).get("equationId"));
    }
    @Test void missingVariablesMapIsRejected() {
        EquationService service = new EquationService();
        String id = service.store("x");
        assertThrows(dev.equations.domain.EquationException.class, () -> service.evaluate(id, null));
    }
}
