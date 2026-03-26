package com.vantryx.api.service;

import com.vantryx.api.dto.StockPredictionDTO;
import com.vantryx.api.model.Product;
import com.vantryx.api.repository.ProductRepository;
import com.vantryx.api.repository.StockMovementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryAIServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @InjectMocks
    private InventoryAIService inventoryAIService;

    @Test
    @DisplayName("Debería predecir estado CRITICAL cuando quedan menos de 3 días de stock")
    void shouldPredictCriticalStatus() {
        // 1. Preparar: Producto con 10 unidades
        Product product = new Product();
        product.setId(1L);
        product.setName("Producto Test");
        product.setCurrentStock(10);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Simulamos que se han vendido 35 unidades en 7 días (Media de 5 al día)
        // 10 stock / 5 al día = 2 días restantes -> CRITICAL
        when(movementRepository.sumOutQuantitySince(eq(1L), any(LocalDateTime.class)))
                .thenReturn(35);

        // 2. Ejecutar
        StockPredictionDTO result = inventoryAIService.predictStockOut(1L);

        // 3. Verificar
        assertEquals(5.0, result.getAverageDailySales());
        assertEquals(2, result.getDaysUntilOutOfStock());
        assertEquals("CRITICAL", result.getStatus());
    }

    @Test
    @DisplayName("Debería predecir estado STABLE cuando no hay ventas recientes")
    void shouldPredictStableWhenNoSales() {
        Product product = new Product();
        product.setId(1L);
        product.setCurrentStock(50);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // 0 ventas en 7 días
        when(movementRepository.sumOutQuantitySince(eq(1L), any(LocalDateTime.class)))
                .thenReturn(0);

        StockPredictionDTO result = inventoryAIService.predictStockOut(1L);

        assertEquals(0.0, result.getAverageDailySales());
        assertEquals(999, result.getDaysUntilOutOfStock());
        assertEquals("STABLE", result.getStatus());
    }

    @Test
    @DisplayName("Debería manejar correctamente cuando el repositorio devuelve null")
    void shouldHandleNullFromRepository() {
        Product product = new Product();
        product.setId(1L);
        product.setCurrentStock(10);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // El repositorio devuelve null (comportamiento común en sum() de SQL sin registros)
        when(movementRepository.sumOutQuantitySince(eq(1L), any(LocalDateTime.class)))
                .thenReturn(null);

        StockPredictionDTO result = inventoryAIService.predictStockOut(1L);

        assertNotNull(result);
        assertEquals(0.0, result.getAverageDailySales());
        assertEquals("STABLE", result.getStatus());
    }
}