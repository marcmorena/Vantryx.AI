package com.vantryx.api.controller;

import com.vantryx.api.config.JwtService;
import com.vantryx.api.service.AIAdvisorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(AIAdvisorController.class)
@AutoConfigureMockMvc(addFilters = false)
class AIAdvisorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIAdvisorService aiAdvisorService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("GET /api/v1/ai/analyze/{id} debería devolver el texto del análisis")
    void shouldReturnProductAnalysis() throws Exception {
        Long productId = 1L;
        String mockAnalysis = "Análisis de IA: El producto tiene una tendencia de venta al alza. Se recomienda aumentar el stock en un 20%.";

        // Mockeamos la respuesta del servicio
        when(aiAdvisorService.getProductAnalysis(productId)).thenReturn(mockAnalysis);

        // Ejecutamos y verificamos
        mockMvc.perform(get("/api/v1/ai/analyze/{productId}", productId))
                .andExpect(status().isOk())
                // Verificamos que el contenido sea EXACTAMENTE el String o que contenga partes clave
                .andExpect(content().string(mockAnalysis))
                .andExpect(content().string(containsString("tendencia de venta")));
    }
}