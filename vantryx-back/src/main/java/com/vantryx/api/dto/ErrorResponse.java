package com.vantryx.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ErrorResponse {
    private String code;
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
