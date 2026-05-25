package com.vantryx.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vantryx.api.dto.ProductDTO;
import com.vantryx.api.model.Category;    // Asegúrate de que esta sea tu ruta real
import com.vantryx.api.model.Supplier;    // Asegúrate de que esta sea tu ruta real
import com.vantryx.api.repository.CategoryRepository;
import com.vantryx.api.repository.ProductRepository;
import com.vantryx.api.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:vantryx_test_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ProductIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private ObjectMapper objectMapper;

    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        // Usamos constructores simples si setName da error,
        // o mejor aún, si tienes @Builder en las entidades, úsalo:

        Category category = new Category();
        category.setName("Procesadores Test");
        category = categoryRepository.save(category);

        Supplier supplier = new Supplier();
        supplier.setName("AMD Test");
        supplier = supplierRepository.save(supplier);

        productDTO = ProductDTO.builder()
                .name("Ryzen 9 5900X")
                .sku("CPU-RYZ-009")
                .description("Procesador de alto rendimiento")
                .purchasePrice(new BigDecimal("350.00"))
                .salePrice(new BigDecimal("520.00"))
                .currentStock(100)
                .minStock(10)
                .leadTime(3)
                .categoryId(category.getId())
                .supplierId(supplier.getId())
                .build();
    }

    @Test
    @DisplayName("Escenario Full: Crear producto como ADMIN y verificar persistencia")
    @WithMockUser(roles = "ADMIN") // Simulamos el rol necesario
    void fullIntegrationFlowTest() throws Exception {

        // 1. POST: Crear el producto
        String responseJson = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku", is("CPU-RYZ-009")))
                .andExpect(jsonPath("$.name", is("Ryzen 9 5900X")))
                .andReturn().getResponse().getContentAsString();

        // 2. DB CHECK: Verificar que realmente está en la base de datos
        assertEquals(1, productRepository.count(), "Debería haber 1 producto en la DB");

        // 3. GET: Listar productos y verificar que aparece el nuevo
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sku", is("CPU-RYZ-009")));
    }

    @Test
    @DisplayName("Seguridad: Debería denegar el acceso a un USER normal para crear productos")
    @WithMockUser(roles = "USER") // Rol insuficiente
    void securityAccessDeniedTest() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isForbidden()); // Esperamos un 403
    }
}
