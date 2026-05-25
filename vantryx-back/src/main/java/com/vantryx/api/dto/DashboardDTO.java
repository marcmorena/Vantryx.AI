package com.vantryx.api.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
    // --- Lo que ya tenías (Inventario) ---
    private long totalProducts;
    private BigDecimal totalInventoryValue;
    private List<ProductDTO> lowStockProducts;
    private long criticalAlertsCount;

    // --- Lo nuevo (Finanzas) ---
    private BigDecimal totalRevenue;      // Ingresos por ventas
    private BigDecimal totalInvestment;   // Gastos en compras recibidas
    private BigDecimal netProfit;         // Beneficio real
}
