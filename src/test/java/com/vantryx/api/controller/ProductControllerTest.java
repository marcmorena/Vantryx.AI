package com.vantryx.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vantryx.api.config.JwtService;
import com.vantryx.api.dto.ProductDTO;
import com.vantryx.api.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.mockito.ArgumentMatchers.eq;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/products debería crear un producto correctamente")
    void shouldCreateProduct() throws Exception {
        // 1. Preparamos el DTO de entrada
        ProductDTO productDTO = ProductDTO.builder()
                .name("Producto Pro")
                .sku("PRO-001")
                .salePrice(new BigDecimal("100.00"))
                .currentStock(10)
                .build();

        // 2. Simulamos el comportamiento del servicio
        when(productService.save(any(ProductDTO.class))).thenReturn(productDTO);

        // 3. Ejecutamos la petición POST
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Producto Pro"))
                .andExpect(jsonPath("$.sku").value("PRO-001"));
    }

    @Test
    @WithMockUser // Usuario por defecto (sin rol ADMIN)
    @DisplayName("GET /api/products debería listar productos")
    void shouldReturnProductList() throws Exception {
        when(productService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(roles = "ADMIN") // Requiere ADMIN
    @DisplayName("PUT /api/products/{id} debería actualizar un producto existente")
    void shouldUpdateProduct() throws Exception {
        Long productId = 1L;

        // 1. Preparamos el DTO con los datos que queremos enviar (simulando que lo hemos modificado)
        ProductDTO updatedProductDTO = ProductDTO.builder()
                .name("Producto Actualizado")
                .sku("PRO-001")
                .salePrice(new BigDecimal("150.00"))
                .currentStock(20)
                // 👇 Añadimos los campos que exigen tus validaciones
                .purchasePrice(new BigDecimal("100.00"))
                .leadTime(5)
                .minStock(5)
                .categoryId(1L)
                .supplierId(2L)
                .build();

        // 2. Simulamos el servicio
        // Usamos eq(productId) porque al usar any() en el segundo parámetro, Mockito lo exige
        when(productService.update(eq(productId), any(ProductDTO.class))).thenReturn(updatedProductDTO);

        // 3. Ejecutamos la petición PUT
        mockMvc.perform(put("/api/products/{id}", productId)
                        .with(csrf()) // Nunca olvides el CSRF en peticiones que modifican datos
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProductDTO)))
                .andExpect(status().isOk()) // Esperamos un 200 OK
                .andExpect(jsonPath("$.name").value("Producto Actualizado"))
                .andExpect(jsonPath("$.salePrice").value(150.00)); // Verificamos el nuevo precio
    }
}