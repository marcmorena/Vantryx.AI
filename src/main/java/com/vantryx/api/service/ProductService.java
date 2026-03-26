package com.vantryx.api.service;

import com.vantryx.api.dto.DashboardDTO;
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

import java.math.BigDecimal;
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
        // 1. Usamos ResourceNotFoundException para la categoría
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + dto.getCategoryId()));

        // 2. Usamos ResourceNotFoundException para el proveedor
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
    public DashboardDTO getDashboardStats() {
        // 1. Obtenemos productos activos
        List<Product> allActiveProducts = productRepository.findAll()
                .stream()
                .filter(p -> !p.isDeleted())
                .collect(Collectors.toList());

        // 2. Calculamos valor total (Precio Venta * Stock)
        BigDecimal totalPotentialRevenue = allActiveProducts.stream()
                .map(p -> p.getSalePrice().multiply(BigDecimal.valueOf(p.getCurrentStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Filtramos y mapeamos los productos con poco stock
        List<ProductDTO> lowStock = allActiveProducts.stream()
                .filter(p -> p.getCurrentStock() <= p.getMinStock())
                .map(productMapper::toDTO)
                .collect(Collectors.toList());

        // 4. Construimos el DTO asegurando que los nombres coincidan con DashboardDTO
        return DashboardDTO.builder()
                .totalProducts((long) allActiveProducts.size()) // Casteo a long por seguridad
                .totalInventoryValue(totalPotentialRevenue)
                .lowStockProducts(lowStock)
                .criticalAlertsCount((long) lowStock.size()) // Usamos el tamaño de la lista de riesgo
                .build();
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
        return productRepository.findAll().stream()
                .filter(p -> !p.isDeleted())
                .filter(p -> p.getCurrentStock() <= p.getMinStock()) // Solo productos en riesgo
                .map(p -> {
                    String status;
                    String suggestion;

                    if (p.getCurrentStock() == 0) {
                        status = "CRÍTICO";
                        suggestion = "Stock agotado. Pedir inmediatamente a " + p.getSupplier().getName();
                    } else if (p.getLeadTime() > 5) {
                        status = "ADVERTENCIA";
                        suggestion = "El proveedor tarda mucho (" + p.getLeadTime() + " días). Adelantar pedido.";
                    } else {
                        status = "STOCK_BAJO";
                        suggestion = "Considerar reposición pronto.";
                    }

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
