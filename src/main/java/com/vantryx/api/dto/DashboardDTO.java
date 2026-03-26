package com.vantryx.api.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
    private long totalProducts;
    private BigDecimal totalInventoryValue;
    private List<ProductDTO> lowStockProducts;
    private long criticalAlertsCount;
}
