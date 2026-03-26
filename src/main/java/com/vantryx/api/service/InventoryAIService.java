package com.vantryx.api.service;

import com.vantryx.api.dto.StockPredictionDTO;
import com.vantryx.api.model.Product;
import com.vantryx.api.repository.ProductRepository;
import com.vantryx.api.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventoryAIService {

    private final StockMovementRepository movementRepository;
    private final ProductRepository productRepository;

    public StockPredictionDTO predictStockOut(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // 1. Calculamos ventas de los últimos 7 días
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Integer totalSold = movementRepository.sumOutQuantitySince(productId, sevenDaysAgo);

        if (totalSold == null) totalSold = 0;

        // 2. Calculamos media diaria (total / 7 días)
        double avgDaily = totalSold / 7.0;

        // 3. Calculamos días restantes (Stock actual / Media diaria)
        int daysLeft = (avgDaily > 0) ? (int) (product.getCurrentStock() / avgDaily) : 999;

        // 4. Definimos el estado
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
