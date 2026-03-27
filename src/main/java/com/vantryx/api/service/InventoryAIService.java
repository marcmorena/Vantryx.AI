package com.vantryx.api.service;

import com.vantryx.api.dto.StockPredictionDTO;
import com.vantryx.api.model.Product;
import com.vantryx.api.repository.ProductRepository;
import com.vantryx.api.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class InventoryAIService {

    private final StockMovementRepository movementRepository;
    private final ProductRepository productRepository;

    public StockPredictionDTO predictStockOut(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // 1. Definimos nuestra ventana de análisis (ej. 7 días)
        int maxDaysToAnalyze = 7;
        LocalDateTime startDate = LocalDateTime.now().minusDays(maxDaysToAnalyze);

        // 2. Obtenemos las ventas totales en ese periodo
        Integer totalSold = movementRepository.sumOutQuantitySince(productId, startDate);
        if (totalSold == null) totalSold = 0;

        // 3. ¡MEJORA! Calculamos los días reales de actividad
        // Contamos los días entre la creación del producto y hoy
        long daysSinceCreation = ChronoUnit.DAYS.between(product.getCreatedAt(), LocalDateTime.now());

        // Usamos el mínimo entre los días que tiene el producto y el máximo de nuestra ventana
        // Usamos Math.max(..., 1) para evitar divisiones por cero si el producto se creó hoy
        long actualDays = Math.min(Math.max(daysSinceCreation, 1), maxDaysToAnalyze);

        // 4. Calculamos media diaria real
        double avgDaily = (double) totalSold / actualDays;

        // 5. Calculamos días restantes
        int daysLeft = (avgDaily > 0) ? (int) (product.getCurrentStock() / avgDaily) : 999;

        // 6. Definimos el estado
        String status = "STABLE";
        if (daysLeft < 3) status = "CRITICAL";
        else if (daysLeft < 7) status = "WARNING";

        return StockPredictionDTO.builder()
                .productId(productId)
                .productName(product.getName())
                .currentStock(product.getCurrentStock())
                .averageDailySales(avgDaily)
                .daysUntilOutOfStock(daysLeft)
                .status(status)
                .build();
    }
}
