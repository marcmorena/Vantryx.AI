package com.vantryx.api.controller;

import com.vantryx.api.config.JwtService;
import com.vantryx.api.dto.StockPredictionDTO;
import com.vantryx.api.service.InventoryAIService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryAIController.class)
@AutoConfigureMockMvc(addFilters = false) // Desactivamos filtros para probar la respuesta de la IA directamente
class InventoryAIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryAIService aiService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("GET /api/ai/predict/{id} debería devolver una predicción de stock coherente")
    void shouldReturnStockPrediction() throws Exception {
        Long productId = 1L;

        // 1. Preparamos el DTO con TUS campos reales
        StockPredictionDTO prediction = StockPredictionDTO.builder()
                .productId(productId)
                .productName("Procesador Quantum X")
                .currentStock(15)
                .averageDailySales(2.5)
                .daysUntilOutOfStock(6) // 15 / 2.5 = 6 días
                .status("WARNING")
                .build();

        when(aiService.predictStockOut(productId)).thenReturn(prediction);

        // 2. Ejecución y Verificación
        mockMvc.perform(get("/api/ai/predict/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.productName").value("Procesador Quantum X"))
                .andExpect(jsonPath("$.currentStock").value(15))
                .andExpect(jsonPath("$.averageDailySales").value(2.5))
                .andExpect(jsonPath("$.daysUntilOutOfStock").value(6))
                .andExpect(jsonPath("$.status").value("WARNING"));
    }
}