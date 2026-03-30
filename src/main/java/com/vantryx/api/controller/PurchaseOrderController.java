package com.vantryx.api.controller;

import com.vantryx.api.dto.PurchaseOrderRequestDTO;
import com.vantryx.api.dto.PurchaseOrderResponseDTO;
import com.vantryx.api.mapper.PurchaseOrderMapper;
import com.vantryx.api.model.PurchaseOrder;
import com.vantryx.api.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final PurchaseOrderMapper purchaseOrderMapper;

    // 1. Crear una nueva orden (Estado: ORDERED)
    @PostMapping
    public ResponseEntity<PurchaseOrderResponseDTO> createOrder(@RequestBody PurchaseOrderRequestDTO dto) {
        PurchaseOrder newOrder = purchaseOrderService.createOrder(dto);
        // Convertimos la entidad a DTO antes de responder
        return new ResponseEntity<>(purchaseOrderMapper.toDTO(newOrder), HttpStatus.CREATED);
    }

    // 2. Marcar una orden como recibida (Actualiza Stock automáticamente)
    @PutMapping("/{id}/receive")
    public ResponseEntity<String> receiveOrder(@PathVariable Long id) {
        purchaseOrderService.receiveOrder(id);
        return ResponseEntity.ok("Orden #" + id + " recibida y stock actualizado con éxito.");
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PurchaseOrderResponseDTO>> getPendingOrders() {
        List<PurchaseOrderResponseDTO> pending = purchaseOrderService.getPendingOrders();
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/generate-suggestions")
    public ResponseEntity<List<PurchaseOrderResponseDTO>> generateSuggestedOrders() {
        // Este método busca alertas, agrupa por proveedor y crea los DRAFTs
        List<PurchaseOrderResponseDTO> suggestions = purchaseOrderService.generateSuggestedOrders();

        return new ResponseEntity<>(suggestions, HttpStatus.CREATED);
    }
}
