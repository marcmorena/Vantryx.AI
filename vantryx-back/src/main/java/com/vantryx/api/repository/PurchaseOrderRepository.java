package com.vantryx.api.repository;

import com.vantryx.api.model.PurchaseOrder;
import com.vantryx.api.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    // Para buscar órdenes por proveedor
    List<PurchaseOrder> findBySupplierId(Long supplierId);

    // Para buscar órdenes por estado (ej: Ver qué órdenes están "PENDIENTES")
    List<PurchaseOrder> findByStatus(OrderStatus status);

    // Busca órdenes pendientes cargando todas las relaciones en una sola query (evita LazyInitializationException)
    @Query("SELECT DISTINCT o FROM PurchaseOrder o " +
           "LEFT JOIN FETCH o.supplier " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product " +
           "WHERE o.status IN :statuses " +
           "ORDER BY o.createdAt DESC")
    List<PurchaseOrder> findPendingWithDetails(@Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT SUM(o.totalAmount) FROM PurchaseOrder o WHERE o.status = :status")
    BigDecimal sumTotalByStatus(@Param("status") OrderStatus status);
}
