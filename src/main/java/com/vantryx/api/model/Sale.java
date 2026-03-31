package com.vantryx.api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Data
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    private Integer quantity;
    private BigDecimal unitPrice; // Precio al que se vendió (por si cambia el PVP del producto)
    private BigDecimal totalAmount;
    private LocalDateTime createdAt = LocalDateTime.now();
}
