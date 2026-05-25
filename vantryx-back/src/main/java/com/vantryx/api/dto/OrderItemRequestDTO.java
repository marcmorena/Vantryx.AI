package com.vantryx.api.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrderItemRequestDTO {
    private Long productId;
    private Integer quantity;
    private BigDecimal priceAtPurchase; // El precio pactado con el proveedor
}
