package com.vantryx.api.repository;

import com.vantryx.api.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    @Query("SELECT SUM(s.totalAmount) FROM Sale s")
    BigDecimal sumAllRevenue();

    @Query("SELECT SUM(s.quantity) FROM Sale s")
    Long countAllItemsSold();
}
