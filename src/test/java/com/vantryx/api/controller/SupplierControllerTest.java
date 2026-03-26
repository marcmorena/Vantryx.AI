package com.vantryx.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vantryx.api.config.JwtService;
import com.vantryx.api.dto.SupplierRequest;
import com.vantryx.api.model.Supplier;
import com.vantryx.api.repository.SupplierRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SupplierController.class)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SupplierRepository supplierRepository; // Mockeamos el repositorio directamente

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/suppliers debería crear un proveedor")
    void shouldCreateSupplier() throws Exception {
        // 1. Datos de entrada usando Setters manuales
        SupplierRequest request = new SupplierRequest();
        request.setName("Tech Supplies S.L.");
        request.setContactName("Juan Pérez");
        request.setEmail("juan@tech.com");
        request.setPhone("600123456");
        request.setAddress("Calle Falsa 123");

        // 2. Entidad de respuesta (Asegúrate de que la entidad Supplier SÍ tenga Builder)
        Supplier savedSupplier = Supplier.builder()
                .id(1L)
                .name("Tech Supplies S.L.")
                .email("juan@tech.com")
                .build();

        when(supplierRepository.save(any(Supplier.class))).thenReturn(savedSupplier);

        // 3. Ejecución
        mockMvc.perform(post("/api/v1/suppliers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Tech Supplies S.L."));
    }
}