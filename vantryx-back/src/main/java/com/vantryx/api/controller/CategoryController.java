package com.vantryx.api.controller;

import com.vantryx.api.dto.CategoryDTO;
import com.vantryx.api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.vantryx.api.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')") // Solo usuarios con rol ADMIN pueden entrar aquí
    public ResponseEntity<CategoryDTO> create(@RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(categoryService.save(dto));
    }

    // NUEVO MÉTODO DE BORRADO SEGURO
    @Transactional
    @DeleteMapping("/{id}/move-to/{newId}")
    public ResponseEntity<Void> deleteAndMove(@PathVariable Long id, @PathVariable Long newId) {
        // 1. Movemos los productos de la categoría que vamos a borrar a la nueva (ej. General)
        productRepository.moveProductsToCategory(id, newId);

        // 2. Borramos la categoría original a través del servicio
        categoryService.delete(id);

        // 3. Devolvemos un 204 (No Content) indicando que todo salió bien
        return ResponseEntity.noContent().build();
    }
}
