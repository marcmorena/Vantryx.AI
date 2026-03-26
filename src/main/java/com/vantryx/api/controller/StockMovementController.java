package com.vantryx.api.controller;

import com.vantryx.api.dto.MovementResponseDTO;
import com.vantryx.api.dto.StockMovementDTO;
import com.vantryx.api.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService movementService;

    @PostMapping("/movement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createMovement(@RequestBody StockMovementDTO dto) {
        movementService.registerMovement(dto);
        return ResponseEntity.ok("Movimiento registrado y stock actualizado con éxito");
    }

    @GetMapping("/product/{productId}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MovementResponseDTO>> getHistory(@PathVariable Long productId) {
        return ResponseEntity.ok(movementService.getProductHistory(productId));
    }
}
