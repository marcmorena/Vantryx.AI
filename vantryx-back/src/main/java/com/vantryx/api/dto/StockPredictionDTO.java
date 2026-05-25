package com.vantryx.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockPredictionDTO {
    private Long productId;
    private String productName;
    private Integer currentStock;
    private Double averageDailySales; // Cuántos vendemos al día
    private Integer daysUntilOutOfStock; // ¡La predicción!
    private String status; // "CRITICAL", "WARNING", "STABLE"
}
