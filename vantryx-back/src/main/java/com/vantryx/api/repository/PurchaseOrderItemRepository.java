package com.vantryx.api.repository;

import com.vantryx.api.model.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

    // Para ver qué compramos exactamente en una orden específica
    List<PurchaseOrderItem> findByPurchaseOrderId(Long purchaseOrderId);
}
