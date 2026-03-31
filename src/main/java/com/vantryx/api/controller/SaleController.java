package com.vantryx.api.controller;

import com.vantryx.api.dto.SaleDTO;
import com.vantryx.api.exception.ResourceNotFoundException;
import com.vantryx.api.model.User;
import com.vantryx.api.repository.UserRepository;
import com.vantryx.api.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {
    private final SaleService saleService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<SaleDTO> createSale(@RequestBody SaleDTO dto, Principal principal) {
        // Principal.getName() nos da el 'username' del usuario logueado
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no autenticado"));

        return ResponseEntity.ok(saleService.registerSale(dto, user));
    }
}
