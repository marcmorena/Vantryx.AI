package com.vantryx.api.repository;

import com.vantryx.api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Podremos buscar todos los productos de una categoría específica
    List<Product> findByCategoryIdAndIsDeletedFalse(Long categoryId);

    // Buscaremos productos por nombre (o parte del nombre)
    List<Product> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name);

    // Buscar un producto específico por ID solo si no está borrado
    Optional<Product> findByIdAndIsDeletedFalse(Long id);

    // Spring "entiende" que debe buscar en la tabla productos donde el ID del proveedor coincida
    List<Product> findBySupplierIdAndIsDeletedFalse(Long supplierId);

    // OPTIMIZACIÓN: El filtro se hace en la base de datos (SQL)
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.currentStock <= p.minStock")
    List<Product> findCriticalStockProducts();

    // Compara dos columnas de la misma fila
    @Query("SELECT p FROM Product p WHERE p.currentStock < p.minStock")
    List<Product> findByCurrentStockLessThanColumnMinStock();

    // Calcula el valor del inventario: sum(stock * precio_compra)
    @Query("SELECT SUM(p.currentStock * p.purchasePrice) FROM Product p")
    BigDecimal calculateInventoryValue();
}
