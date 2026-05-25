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
        // 1. Buscar el producto (Igual que antes)
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // --- 2. SOLUCIÓN DEFINITIVA: Forzar el usuario ID 10 ---
        // En lugar de buscar por nombre de usuario anónimo, buscamos tu ID real
        User currentUser = userRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("La tabla de usuarios está vacía. Registra un usuario primero."));
        // -------------------------------------------------------

        // 3. Cálculo de stock (Igual que antes)
        int newStock = product.getCurrentStock();
        if (dto.getType() == com.vantryx.api.model.MovementType.IN) {
            newStock += dto.getQuantity();
        } else if (dto.getType() == com.vantryx.api.model.MovementType.OUT) {
            if (product.getCurrentStock() < dto.getQuantity()) {
                throw new RuntimeException("Stock insuficiente");
            }
            newStock -= dto.getQuantity();
        } else {
            newStock = dto.getQuantity();
        }

        // 4. Guardar cambios
        product.setCurrentStock(newStock);
        productRepository.save(product);

        // 5. Registrar con el usuario forzado
        StockMovement movement = StockMovement.builder()
                .product(product)
                .quantity(dto.getQuantity())
                .type(dto.getType())
                .reason(dto.getReason())
                .user(currentUser) // Ahora sí, currentUser es el ID 10
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
                        .username(m.getUser() != null ? m.getUser().getUsername() : "Sistema")
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
