package com.vantryx.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El SKU es obligatorio")
    private String sku;

    private String description;

    // --- Campos Financieros y de IA ---

    @NotNull(message = "El precio de venta es obligatorio")
    @Min(value = 0)
    private BigDecimal salePrice; // Lo que paga el cliente

    @NotNull(message = "El precio de compra es obligatorio")
    @Min(value = 0)
    private BigDecimal purchasePrice; // Lo que nos cuesta a nosotros

    @NotNull(message = "El tiempo de entrega (Lead Time) es obligatorio")
    @Min(value = 1, message = "El tiempo de entrega debe ser al menos de 1 día")
    private Integer leadTime; // Días que tarda el proveedor

    // --- Gestión de Stock ---

    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0)
    private Integer currentStock;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0)
    private Integer minStock;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    private String categoryName;

    @NotNull(message = "El ID del proveedor es obligatorio")
    private Long supplierId;

    private String supplierName;
}