package com.vantryx.api.controller;

import com.vantryx.api.dto.SupplierRequest;
import com.vantryx.api.model.Supplier;
import com.vantryx.api.repository.SupplierRepository;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/suppliers")
@Tag(name = "Suppliers", description = "Gestión de proveedores de Vantryx")
public class SupplierController {

    private final SupplierRepository supplierRepository;

    public SupplierController(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    // 1. LISTAR TODOS (Ruta: GET /api/v1/suppliers)
    @GetMapping
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        return ResponseEntity.ok(supplierRepository.findAll());
    }

    // 2. BUSCAR POR ID (Ruta: GET /api/v1/suppliers/{id})
    // IMPORTANTE: Asegúrate de que tiene el /{id}
    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplier(@PathVariable("id") Long id) {
        return supplierRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. CREAR (Ruta: POST /api/v1/suppliers)
    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@RequestBody SupplierRequest request) {
        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .contactName(request.getContactName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        return ResponseEntity.ok(supplierRepository.save(supplier));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplierDetails) {
        return supplierRepository.findById(id)
                .map(supplier -> {
                    // Actualizamos los campos necesarios
                    supplier.setName(supplierDetails.getName());
                    supplier.setContactName(supplierDetails.getContactName());
                    supplier.setEmail(supplierDetails.getEmail());
                    supplier.setPhone(supplierDetails.getPhone());
                    supplier.setAddress(supplierDetails.getAddress());

                    Supplier updated = supplierRepository.save(supplier);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        if (supplierRepository.existsById(id)) {
            supplierRepository.deleteById(id);
            // Devolvemos 204 (No Content) que es el estándar para borrados exitosos
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }


}
