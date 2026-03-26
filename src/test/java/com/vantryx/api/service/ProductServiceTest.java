package com.vantryx.api.service;

import com.vantryx.api.model.Product;
import com.vantryx.api.model.Supplier;
import com.vantryx.api.repository.ProductRepository;
import com.vantryx.api.dto.StockAlertDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Usamos Mockito para no necesitar la DB real
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository; // Simulamos el repositorio

    @InjectMocks
    private ProductService productService; // Spring inyecta el mock arriba en este servicio

    @Test
    @DisplayName("Debería detectar productos con stock bajo")
    void shouldDetectLowStockProducts() {
        // 1. Creamos el Proveedor (Indispensable para el LeadTime y alertas)
        Supplier mockSupplier = new Supplier();
        mockSupplier.setId(1L);
        mockSupplier.setName("Proveedor Test");

        // 2. Creamos el Producto manualmente (No usamos Builder por la herencia)
        Product lowStockProduct = new Product();
        lowStockProduct.setName("Chip Test");
        lowStockProduct.setSku("SKU-001");
        lowStockProduct.setCurrentStock(5);
        lowStockProduct.setMinStock(10);
        lowStockProduct.setLeadTime(2);
        lowStockProduct.setSalePrice(new BigDecimal("100.00"));
        lowStockProduct.setPurchasePrice(new BigDecimal("50.00"));
        lowStockProduct.setSupplier(mockSupplier);

        // 3. Establecemos el borrado lógico heredado de BaseEntity
        // Si en BaseEntity el campo se llama 'isDeleted', el setter es 'setDeleted'
        lowStockProduct.setDeleted(false);

        // 4. Configuramos el Mock (Tu servicio usa findAll() y luego filtra por Java)
        when(productRepository.findAll()).thenReturn(List.of(lowStockProduct));

        // 5. Ejecutamos la lógica de Vantryx
        List<StockAlertDTO> alerts = productService.getInventoryAlerts();

        // 6. Verificaciones
        assertFalse(alerts.isEmpty(), "La lista de alertas no debería estar vacía");
        assertEquals(1, alerts.size());
        assertEquals("Chip Test", alerts.get(0).getProductName());
    }
}