package com.vantryx.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockAlertDTO {
    private Long productId;
    private String productName;
    private Integer currentStock;
    private Integer minStock;
    private Integer leadTime; // Días que tarda el proveedor
    private String supplierName;
    private String status;
    private String suggestion; // Ejemplo: "Pedido urgente necesario"
}
