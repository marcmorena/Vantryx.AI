package com.vantryx.api.mapper;

import com.vantryx.api.dto.MovementResponseDTO;
import com.vantryx.api.model.StockMovement;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public MovementResponseDTO toDTO(StockMovement movement) {
        if (movement == null) return null;

        return MovementResponseDTO.builder()
                .id(movement.getId())
                .quantity(movement.getQuantity())
                .type(movement.getType()) // Al ser el mismo Enum en ambos lados, mapea directo
                .reason(movement.getReason())
                // Sacamos el username del objeto User vinculado al movimiento
                .username(movement.getUser() != null ? movement.getUser().getUsername() : "Sistema")
                .createdAt(movement.getCreatedAt())
                .build();
    }
}
