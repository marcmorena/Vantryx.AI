package com.vantryx.api.service;

import com.vantryx.api.dto.ProductDTO;
import com.vantryx.api.dto.StockAlertDTO;
import com.vantryx.api.exception.ResourceNotFoundException;
import com.vantryx.api.mapper.ProductMapper;
import com.vantryx.api.model.Category;
import com.vantryx.api.model.Product;
import com.vantryx.api.model.Supplier;
import com.vantryx.api.repository.CategoryRepository;
import com.vantryx.api.repository.ProductRepository;
import com.vantryx.api.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<ProductDTO> findAll() {
        return productRepository.findAll()
                .stream()
                .filter(p -> !p.isDeleted())
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO save(ProductDTO dto) {
        // Validamos que los IDs vengan en el JSON antes de buscar
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("El categoryId es obligatorio");
        }
        if (dto.getSupplierId() == null) {
            throw new IllegalArgumentException("El supplierId es obligatorio");
        }

        // 1. Buscamos la categoría
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + dto.getCategoryId()));

        // 2. Buscamos el proveedor
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + dto.getSupplierId()));

        Product product = productMapper.toEntity(dto);
        product.setCategory(category);
        product.setSupplier(supplier);

        return productMapper.toDTO(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        // 1. Buscamos el producto (lanzamos 404 si no existe o ya está borrado)
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        // 2. En lugar de borrar, cambiamos el estado
        product.setDeleted(true);

        // 3. Guardamos los cambios
        productRepository.save(product);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        // 1. Verificamos si el producto existe
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        // 2. Buscamos categoría y proveedor (como en el save)
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        // 3. Actualizamos los campos manualmente
        existingProduct.setName(dto.getName());
        existingProduct.setSku(dto.getSku()); // Ten cuidado aquí: si cambias el SKU a uno que ya existe, fallará igual.
        existingProduct.setDescription(dto.getDescription());
        existingProduct.setSalePrice(dto.getSalePrice());
        existingProduct.setPurchasePrice(dto.getPurchasePrice());
        existingProduct.setLeadTime(dto.getLeadTime());
        existingProduct.setCurrentStock(dto.getCurrentStock());
        existingProduct.setMinStock(dto.getMinStock());
        existingProduct.setCategory(category);
        existingProduct.setSupplier(supplier);

        return productMapper.toDTO(productRepository.save(existingProduct));
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(name)
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findBySupplier(Long supplierId) {
        // 3. También aquí, si el proveedor no existe, lanzamos la excepción específica
        if (!supplierRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException("El proveedor con ID " + supplierId + " no existe.");
        }

        return productRepository.findBySupplierIdAndIsDeletedFalse(supplierId)
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockAlertDTO> getInventoryAlerts() {
        return productRepository.findCriticalStockProducts()
                .stream()
                .map(p -> {
                    String status = (p.getCurrentStock() == 0) ? "CRÍTICO" :
                            (p.getLeadTime() > 5) ? "ADVERTENCIA" : "STOCK_BAJO";

                    String suggestion = (p.getCurrentStock() == 0) ?
                            "Stock agotado. Pedir inmediatamente a " + p.getSupplier().getName() :
                            "Considerar reposición pronto.";

                    return StockAlertDTO.builder()
                            .productId(p.getId())
                            .productName(p.getName())
                            .currentStock(p.getCurrentStock())
                            .minStock(p.getMinStock())
                            .leadTime(p.getLeadTime())
                            .supplierName(p.getSupplier() != null ? p.getSupplier().getName() : "Sin Proveedor")
                            .status(status)
                            .suggestion(suggestion)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
