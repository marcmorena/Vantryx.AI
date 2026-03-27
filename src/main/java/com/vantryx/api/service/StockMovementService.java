package com.vantryx.api.service;

import com.vantryx.api.dto.MovementResponseDTO;
import com.vantryx.api.dto.StockMovementDTO;
import com.vantryx.api.exception.ResourceNotFoundException;
import com.vantryx.api.model.Product;
import com.vantryx.api.model.StockMovement;
import com.vantryx.api.model.User;
import com.vantryx.api.repository.ProductRepository;
import com.vantryx.api.repository.StockMovementRepository;
import com.vantryx.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public void registerMovement(StockMovementDTO dto) {
        // 1. Buscar el producto
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // --- 2. AUDITORÍA: Extraer usuario autenticado ---
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + currentUsername));
        // -------------------------------------------------

        // 3. Calcular el nuevo stock
        int newStock = product.getCurrentStock();
        if (dto.getType() == com.vantryx.api.model.MovementType.IN) {
            newStock += dto.getQuantity();
        } else if (dto.getType() == com.vantryx.api.model.MovementType.OUT) {
            if (product.getCurrentStock() < dto.getQuantity()) {
                throw new RuntimeException("Stock insuficiente para realizar la salida");
            }
            newStock -= dto.getQuantity();
        } else { // ADJUSTMENT
            newStock = dto.getQuantity(); // En un ajuste, la cantidad enviada suele ser el total real
        }

        // 4. Actualizar el producto
        product.setCurrentStock(newStock);
        productRepository.save(product);

        // 5. Registrar el movimiento en el historial vinculando al usuario
        StockMovement movement = StockMovement.builder()
                .product(product)
                .quantity(dto.getQuantity())
                .type(dto.getType())
                .reason(dto.getReason())
                .user(currentUser) // <--- ¡Aquí queda registrado quién hizo la acción!
                .build();

        movementRepository.save(movement);
    }

    @Transactional(readOnly = true)
    public List<MovementResponseDTO> getProductHistory(Long productId) {
        return movementRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(m -> MovementResponseDTO.builder()
                        .id(m.getId())
                        .quantity(m.getQuantity())
                        .type(m.getType())
                        .reason(m.getReason())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
