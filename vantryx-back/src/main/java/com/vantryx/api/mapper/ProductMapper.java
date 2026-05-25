package com.vantryx.api.mapper;

import com.vantryx.api.dto.ProductDTO;
import com.vantryx.api.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDTO toDTO(Product entity) {
        if (entity == null) return null;
        return ProductDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .sku(entity.getSku())
                .description(entity.getDescription())
                .salePrice(entity.getSalePrice())
                .purchasePrice(entity.getPurchasePrice()) // Añadido para que se vea en el DTO
                .leadTime(entity.getLeadTime())           // Añadido para que se vea en el DTO
                .currentStock(entity.getCurrentStock())
                .minStock(entity.getMinStock())
                // Mapeo de Categoría
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                // --- NUEVO: Mapeo de Proveedor ---
                .supplierId(entity.getSupplier() != null ? entity.getSupplier().getId() : null)
                .supplierName(entity.getSupplier() != null ? entity.getSupplier().getName() : null)
                .build();
    }

    public Product toEntity(ProductDTO dto) {
        if (dto == null) return null;

        Product product = Product.builder()
                .name(dto.getName())
                .sku(dto.getSku())
                .description(dto.getDescription())
                .salePrice(dto.getSalePrice())
                .purchasePrice(dto.getPurchasePrice())
                .leadTime(dto.getLeadTime())
                .currentStock(dto.getCurrentStock())
                .minStock(dto.getMinStock())
                .build();

        // Asignamos el ID manualmente fuera del builder
        product.setId(dto.getId());

        return product;
    }
}
