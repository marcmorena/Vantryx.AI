package com.vantryx.api.repository;

import com.vantryx.api.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Buscaremos categorías por nombre para evitar duplicados
    Optional<Category> findByName(String name);

    // Método rápido para saber si un nombre ya existe
    boolean existsByName(String name);
}
