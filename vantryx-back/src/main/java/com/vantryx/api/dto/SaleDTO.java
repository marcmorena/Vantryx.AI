package com.vantryx.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SaleDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
