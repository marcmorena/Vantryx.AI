package com.vantryx.api.exception;

import com.vantryx.api.dto.ErrorResponse;
import com.vantryx.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    // 1. Creamos un controlador "ficticio" solo para el test
    @RestController
    static class TestController {
        @GetMapping("/test/runtime")
        public void throwRuntime() { throw new RuntimeException("Error de base de datos"); }

        @GetMapping("/test/not-found")
        public void throwNotFound() { throw new ResourceNotFoundException("Producto 99 no existe"); }
    }

    @BeforeEach
    void setup() {
        // Configuramos MockMvc para que use nuestro Handler
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Debería retornar 404 cuando se lanza ResourceNotFoundException")
    void handleNotFoundTest() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.error", is("Recurso no encontrado")))
                .andExpect(jsonPath("$.message", is("Producto 99 no existe")))
                .andExpect(jsonPath("$.path", is("/test/not-found")));
    }

    @Test
    @DisplayName("Debería retornar 400 cuando se lanza RuntimeException")
    void handleRuntimeTest() throws Exception {
        mockMvc.perform(get("/test/runtime"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("BUSINESS_ERROR")))
                .andExpect(jsonPath("$.message", is("Error de base de datos")));
    }
}