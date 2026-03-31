package com.vantryx.api.service;

import com.vantryx.api.dto.SaleDTO;
import com.vantryx.api.exception.BusinessException;
import com.vantryx.api.exception.ResourceNotFoundException;
import com.vantryx.api.mapper.SaleMapper;
import com.vantryx.api.model.*;
import com.vantryx.api.repository.ProductRepository;
import com.vantryx.api.repository.SaleRepository;
import com.vantryx.api.repository.StockMovementRepository;
import com.vantryx.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SaleService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository movementRepository;
    private final SaleMapper saleMapper; // <--- No olvides este
    private final UserRepository userRepository;

    @Transactional
    public SaleDTO registerSale(SaleDTO dto, User user) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // 1. Validar Stock
        if (product.getCurrentStock() < dto.getQuantity()) {
            throw new BusinessException("Stock insuficiente. Disponible: " + product.getCurrentStock());
        }

        // 2. Actualizar Stock del Producto
        product.setCurrentStock(product.getCurrentStock() - dto.getQuantity());
        productRepository.save(product);

        // 3. Registrar Movimiento de Inventario (Tipo OUT)
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setUser(user);
        movement.setQuantity(dto.getQuantity());
        movement.setType(MovementType.OUT);
        movement.setReason("Venta #ID-PROX");
        movementRepository.save(movement);

        // 4. Guardar la Venta
        Sale sale = new Sale();
        sale.setProduct(product);
        sale.setUser(user);
        sale.setQuantity(dto.getQuantity());
        sale.setUnitPrice(product.getSalePrice());
        sale.setTotalAmount(product.getSalePrice().multiply(BigDecimal.valueOf(dto.getQuantity())));

        return saleMapper.toDTO(saleRepository.save(sale));
    }
}
