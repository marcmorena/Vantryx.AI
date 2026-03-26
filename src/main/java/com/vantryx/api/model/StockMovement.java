package com.vantryx.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_movements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity; // Positivo para entradas, negativo para salidas

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type; // IN, OUT, ADJUSTMENT

    private String reason; // Ej: "Venta online", "Reposición de proveedor"
}
