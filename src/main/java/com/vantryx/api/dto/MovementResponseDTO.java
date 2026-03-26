package com.vantryx.api.dto;

import com.vantryx.api.model.MovementType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MovementResponseDTO {
    private Long id;
    private Integer quantity;
    private MovementType type;
    private String reason;
    private LocalDateTime createdAt;
}
