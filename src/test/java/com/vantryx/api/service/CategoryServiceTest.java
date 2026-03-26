package com.vantryx.api.service;

import com.vantryx.api.dto.CategoryDTO;
import com.vantryx.api.mapper.CategoryMapper;
import com.vantryx.api.model.Category;
import com.vantryx.api.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("findAll debería filtrar las categorías marcadas como borradas")
    void shouldFilterDeletedCategories() {
        // 1. Preparar: Una activa y una borrada
        Category active = new Category();
        active.setName("Electrónica");
        active.setDeleted(false);

        Category deleted = new Category();
        deleted.setName("Antigüedades");
        deleted.setDeleted(true);

        when(categoryRepository.findAll()).thenReturn(List.of(active, deleted));
        when(categoryMapper.toDTO(active)).thenReturn(
                CategoryDTO.builder()
                        .id(1L)
                        .name("Electrónica")
                        .build()
        );

        // 2. Ejecutar
        List<CategoryDTO> result = categoryService.findAll();

        // 3. Verificar: Solo debe haber 1 en el resultado
        assertEquals(1, result.size());
        assertEquals("Electrónica", result.get(0).getName());
        verify(categoryMapper, times(1)).toDTO(any());
    }

    @Test
    @DisplayName("save debería lanzar excepción si el nombre ya existe")
    void shouldThrowExceptionWhenNameExists() {
        CategoryDTO dto = CategoryDTO.builder().name("Hardware").build();

        // Simulamos que ya existe en la DB
        when(categoryRepository.existsByName("Hardware")).thenReturn(true);

        // Verificar que lanza la excepción con el mensaje correcto
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            categoryService.save(dto);
        });

        assertEquals("La categoría 'Hardware' ya existe.", ex.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("save debería guardar la categoría si el nombre es nuevo")
    void shouldSaveCategorySuccessfully() {
        CategoryDTO dto = CategoryDTO.builder().name("Software").build();
        Category entity = new Category();
        entity.setName("Software");

        when(categoryRepository.existsByName("Software")).thenReturn(false);
        when(categoryMapper.toEntity(dto)).thenReturn(entity);
        when(categoryRepository.save(entity)).thenReturn(entity);
        when(categoryMapper.toDTO(entity)).thenReturn(dto);

        CategoryDTO result = categoryService.save(dto);

        assertNotNull(result);
        assertEquals("Software", result.getName());
        verify(categoryRepository).save(entity);
    }
}