package com.vantryx.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vantryx.api.config.JwtService;
import com.vantryx.api.dto.StockMovementDTO;
import com.vantryx.api.dto.MovementResponseDTO;
import com.vantryx.api.service.StockMovementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockMovementController.class)
@EnableMethodSecurity // Para que respete el @PreAuthorize("hasRole('ADMIN')")
class StockMovementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StockMovementService movementService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/inventory/movement debería registrar un movimiento correctamente")
    void shouldRegisterMovement() throws Exception {
        // 1. Preparamos el DTO de entrada con los campos REALES de tu clase
        StockMovementDTO dto = StockMovementDTO.builder()
                .productId(1L)
                .quantity(10)
                .type(com.vantryx.api.model.MovementType.IN) // Usa tu Enum aquí
                .reason("Compra a proveedor")
                .build();

        // 2. Mock del servicio (debe coincidir con la firma del método en el Service)
        doNothing().when(movementService).registerMovement(any(StockMovementDTO.class));

        // 3. Ejecución
        mockMvc.perform(post("/api/inventory/movement")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Movimiento registrado y stock actualizado con éxito"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/inventory/product/{id}/history debería devolver el historial")
    void shouldGetProductHistory() throws Exception {
        Long productId = 1L;

        // 1. Preparamos la respuesta usando el Enum MovementType.OUT
        MovementResponseDTO response = MovementResponseDTO.builder()
                .id(100L)
                .quantity(5)
                .type(com.vantryx.api.model.MovementType.OUT) // <--- Cambiado de "OUT" al Enum
                .createdAt(LocalDateTime.now())
                .build();

        when(movementService.getProductHistory(productId)).thenReturn(List.of(response));

        // 2. Ejecución y Verificación
        mockMvc.perform(get("/api/inventory/product/{id}/history", productId))
                .andExpect(status().isOk())
                // En el JSON de respuesta, el Enum se convierte a String "OUT" automáticamente
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].type").value("OUT"))
                .andExpect(jsonPath("$[0].quantity").value(5));
    }

    @Test
    @WithMockUser(roles = "USER") // Probamos con un usuario sin permisos
    @DisplayName("GET /history debería fallar si el usuario no es ADMIN")
    void shouldFailWhenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/inventory/product/1/history"))
                .andExpect(status().isBadRequest()); // Recuerda que tu app devuelve 400 por el Access Denied
    }
}