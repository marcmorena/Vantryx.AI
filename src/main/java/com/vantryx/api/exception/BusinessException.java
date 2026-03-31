package com.vantryx.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Esto hará que devuelva un Error 400
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
