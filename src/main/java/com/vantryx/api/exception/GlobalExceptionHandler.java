package com.vantryx.api.exception;

import com.vantryx.api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Errores de Negocio (400)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        return buildResponseEntity("BUSINESS_ERROR", "Error de lógica de negocio", ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    // 2. Recurso No Encontrado (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponseEntity("RESOURCE_NOT_FOUND", "Recurso no encontrado", ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    // 3. Errores de Validación (400 con lista de fallos)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Concatenamos los errores de los campos para el mensaje
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(" | "));

        return buildResponseEntity("VALIDATION_ERROR", "Error en los campos del formulario", details, HttpStatus.BAD_REQUEST, request);
    }

    // 4. Errores de Permisos / Seguridad (403)
    @ExceptionHandler({
            org.springframework.security.access.AccessDeniedException.class,
            org.springframework.security.authorization.AuthorizationDeniedException.class
    })
    public ResponseEntity<ErrorResponse> handleAccessDenied(Exception ex, HttpServletRequest request) {
        return buildResponseEntity("ACCESS_DENIED", "No tienes permisos para esta acción", ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    // MÉTODO AUXILIAR - Ajustado a tu ErrorResponse
    private ResponseEntity<ErrorResponse> buildResponseEntity(String code, String errorLabel, String message, HttpStatus status, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .code(code)
                .timestamp(LocalDateTime.now()) // Tu DTO usa LocalDateTime
                .status(status.value())
                .error(errorLabel)
                .message(message)
                .path(request.getRequestURI()) // Captura la URL automáticamente
                .build();
        return new ResponseEntity<>(response, status);
    }
}