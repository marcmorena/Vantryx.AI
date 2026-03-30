package com.vantryx.api.mapper;

import com.vantryx.api.dto.OrderItemResponseDTO;
import com.vantryx.api.dto.PurchaseOrderResponseDTO;
import com.vantryx.api.model.PurchaseOrder;
import com.vantryx.api.model.PurchaseOrderItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PurchaseOrderMapper {

    public PurchaseOrderResponseDTO toDTO(PurchaseOrder order) {
        if (order == null) return null;

        return PurchaseOrderResponseDTO.builder()
                .id(order.getId())
                .supplierName(order.getSupplier().getName())
                .username(order.getUser().getUsername())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .deliveryDate(order.getDeliveryDate())
                .items(order.getItems().stream()
                        .map(this::toItemDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    private OrderItemResponseDTO toItemDTO(PurchaseOrderItem item) {
        return OrderItemResponseDTO.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .build();
    }
}
