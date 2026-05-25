package com.vantryx.api.service;

import com.vantryx.api.dto.StockMovementDTO;
import com.vantryx.api.dto.MovementResponseDTO;
import com.vantryx.api.model.Product;
import com.vantryx.api.model.StockMovement;
import com.vantryx.api.model.MovementType;
import com.vantryx.api.repository.ProductRepository;
import com.vantryx.api.repository.StockMovementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @InjectMocks
    private StockMovementService stockMovementService;

    @Test
    @DisplayName("Debería sumar stock cuando el movimiento es tipo IN")
    void shouldIncreaseStockOnInMovement() {
        // 1. Instanciamos con "new" y usamos Setters
        Product product = new Product();
        product.setId(1L);
        product.setCurrentStock(10);

        StockMovementDTO dto = StockMovementDTO.builder()
                .productId(1L)
                .quantity(5)
                .type(MovementType.IN)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // 2. Ejecutar
        stockMovementService.registerMovement(dto);

        // 3. Verificar resultados
        assertEquals(15, product.getCurrentStock(), "El stock debería haber subido de 10 a 15");
        verify(productRepository, times(1)).save(product);
        verify(movementRepository, times(1)).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("Debería restar stock cuando el movimiento es tipo OUT y hay stock suficiente")
    void shouldDecreaseStockOnOutMovement() {
        Product product = new Product();
        product.setId(1L);
        product.setCurrentStock(20);

        StockMovementDTO dto = StockMovementDTO.builder()
                .productId(1L)
                .quantity(5)
                .type(MovementType.OUT)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        stockMovementService.registerMovement(dto);

        assertEquals(15, product.getCurrentStock(), "El stock debería haber bajado de 20 a 15");
        verify(productRepository).save(product);
        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción si se intenta un OUT con stock insuficiente")
    void shouldThrowExceptionWhenOutMovementExceedsStock() {
        Product product = new Product();
        product.setId(1L);
        product.setCurrentStock(5); // Solo hay 5

        StockMovementDTO dto = StockMovementDTO.builder()
                .productId(1L)
                .quantity(10) // Intentamos sacar 10
                .type(MovementType.OUT)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            stockMovementService.registerMovement(dto);
        });

        assertEquals("Stock insuficiente para realizar la salida", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
        verify(movementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("Debería devolver el historial de un producto mapeado a DTOs")
    void shouldGetProductHistory() {
        Product product = new Product();
        product.setId(1L);

        StockMovement movement = new StockMovement();
        movement.setId(100L);
        movement.setProduct(product);
        movement.setQuantity(10);
        movement.setType(MovementType.IN);
        movement.setCreatedAt(LocalDateTime.now());

        when(movementRepository.findByProductIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(movement));

        List<MovementResponseDTO> result = stockMovementService.getProductHistory(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals(10, result.get(0).getQuantity());
    }
}