package com.vantryx.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String sku; // Código único de producto (ej: PROD-001)

    private String description;

    @Column(nullable = false)
    private BigDecimal salePrice; // Siempre usar BigDecimal para dinero, nunca double o float

    @Column(nullable = false)
    private BigDecimal purchasePrice; // Precio de compra

    @Column(nullable = false)
    private Integer leadTime;   // Días de espera del proveedor (ej: 3, 5, 10)

    @Column(nullable = false)
    private Integer currentStock;

    @Column(nullable = false)
    private Integer minStock; // Para que la IA nos avise cuando haya poco

    @ManyToOne(fetch = FetchType.LAZY) // Lazy para mejorar el rendimiento
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
}
