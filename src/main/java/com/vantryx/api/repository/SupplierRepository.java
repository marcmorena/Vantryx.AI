package com.vantryx.api.repository;

import com.vantryx.api.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    // Aquí podrías añadir métodos personalizados en el futuro,
    // como buscar por nombre o por email.
}
