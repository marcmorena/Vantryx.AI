package com.vantryx.api.dto;

import com.vantryx.api.model.MovementType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementDTO {
    private Long productId;
    private Integer quantity;
    private MovementType type;
    private String reason;
}
