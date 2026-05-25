package com.vantryx.api.repository;

import com.vantryx.api.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId);
    // Buscamos movimientos de un tipo específico para un producto en los últimos X días
    @Query("SELECT SUM(s.quantity) FROM StockMovement s WHERE s.product.id = :productId " +
            "AND s.type = 'OUT' AND s.createdAt >= :startDate")
    Integer sumOutQuantitySince(Long productId, java.time.LocalDateTime startDate);
}
