package com.vantryx.api.dto;

import com.vantryx.api.model.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PurchaseOrderResponseDTO {
    private Long id;
    private String supplierName;
    private String username; // Quién hizo el pedido
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private List<OrderItemResponseDTO> items;
}
