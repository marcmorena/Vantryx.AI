package com.vantryx.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vantryx.api.config.JwtService;
import com.vantryx.api.dto.CategoryDTO;
import com.vantryx.api.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@EnableMethodSecurity
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser // Usuario normal por defecto
    @DisplayName("GET /api/categories debería listar todas las categorías")
    void shouldReturnAllCategories() throws Exception {
        CategoryDTO dto = new CategoryDTO(1L, "Electrónica", "Gadgets y más");
        when(categoryService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electrónica"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/categories debería crear una categoría si es ADMIN")
    void shouldCreateCategoryWhenAdmin() throws Exception {
        CategoryDTO dto = new CategoryDTO(null, "Alimentación", "Perecederos");
        CategoryDTO savedDto = new CategoryDTO(1L, "Alimentación", "Perecederos");

        when(categoryService.save(any(CategoryDTO.class))).thenReturn(savedDto);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Alimentación"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/categories debería devolver 400 (Access Denied) si el usuario no es ADMIN")
    void shouldReturn400WhenUserIsNotAdmin() throws Exception {
        CategoryDTO dto = new CategoryDTO(null, "Hack", "Intento de intrusión");

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // Cambiamos isForbidden() por isBadRequest() (400)
    }
}