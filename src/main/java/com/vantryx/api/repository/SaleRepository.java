package com.vantryx.api.repository;

import com.vantryx.api.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    // Aquí podrías añadir métodos como buscar ventas por fecha o por usuario más adelante
}
