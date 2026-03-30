package com.vantryx.api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PurchaseOrderRequestDTO {
    private Long supplierId;
    private List<OrderItemRequestDTO> items;
}
