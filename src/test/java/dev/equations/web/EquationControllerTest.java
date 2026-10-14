package dev.equations.web;

import dev.equations.service.EquationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EquationController.class)
class EquationControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean EquationService service;

    @Test void successfulStore() throws Exception {
        when(service.store("x+1")).thenReturn("1");
        mvc.perform(post("/api/equations/store")
                .contentType("application/json").content("{\"equation\":\"x+1\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.equationId").value("1"));
    }
    @Test void invalidStore() throws Exception {
        mvc.perform(post("/api/equations/store")
                .contentType("application/json").content("{\"equation\":\"\"}"))
            .andExpect(status().isBadRequest());
    }
    @Test void nonexistentEquation() throws Exception {
        when(service.evaluate(eq("55"), any())).thenThrow(new java.util.NoSuchElementException("Equation not found: 55"));
        mvc.perform(post("/api/equations/55/evaluate")
                .contentType("application/json").content("{\"variables\":{}}"))
            .andExpect(status().isNotFound());
    }
    @Test void missingVariables() throws Exception {
        mvc.perform(post("/api/equations/55/evaluate")
                .contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest());
    }
}
