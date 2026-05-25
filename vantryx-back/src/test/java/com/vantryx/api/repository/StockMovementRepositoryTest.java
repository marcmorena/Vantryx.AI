package com.vantryx.api.repository;

import com.vantryx.api.model.MovementType;
import com.vantryx.api.model.Product;
import com.vantryx.api.model.StockMovement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StockMovementRepositoryTest {

    @Autowired
    private StockMovementRepository movementRepository;

    @Autowired
    private ProductRepository productRepository;

    // Método auxiliar para crear un producto válido con todos los campos obligatorios
    private Product createValidProduct(String name, String sku) {
        Product p = new Product();
        p.setName(name);
        p.setSku(sku);
        p.setCurrentStock(50);
        p.setMinStock(10);
        p.setLeadTime(5);
        p.setPurchasePrice(new BigDecimal("100.00"));
        p.setSalePrice(new BigDecimal("150.00"));
        return productRepository.save(p);
    }

    @Test
    @DisplayName("Debería calcular correctamente la suma de salidas (OUT) en un rango de fechas")
    void shouldSumOutQuantitySince() {
        // 1. Preparar: Usamos el método auxiliar
        Product product = createValidProduct("Ryzen 7", "CPU-001");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        // 2. Insertamos movimientos
        StockMovement m1 = new StockMovement();
        m1.setProduct(product);
        m1.setQuantity(10);
        m1.setType(MovementType.OUT);
        m1.setCreatedAt(now.minusDays(2));
        movementRepository.save(m1);

        StockMovement m2 = new StockMovement();
        m2.setProduct(product);
        m2.setQuantity(5);
        m2.setType(MovementType.OUT);
        m2.setCreatedAt(now.minusDays(5));
        movementRepository.save(m2);

        // Entrada (No debe sumarse)
        StockMovement m3 = new StockMovement();
        m3.setProduct(product);
        m3.setQuantity(100);
        m3.setType(MovementType.IN);
        m3.setCreatedAt(now.minusDays(1));
        movementRepository.save(m3);

        // 3. Ejecutar
        Integer totalSold = movementRepository.sumOutQuantitySince(product.getId(), sevenDaysAgo);

        // 4. Verificar
        assertNotNull(totalSold);
        assertEquals(15, totalSold);
    }

    @Test
    @DisplayName("Debería devolver null o cero cuando no hay movimientos")
    void shouldReturnNullWhenNoMovementsInRange() {
        Product product = createValidProduct("Teclado", "KB-001");

        Integer total = movementRepository.sumOutQuantitySince(product.getId(), LocalDateTime.now().minusDays(1));

        assertTrue(total == null || total == 0);
    }
}