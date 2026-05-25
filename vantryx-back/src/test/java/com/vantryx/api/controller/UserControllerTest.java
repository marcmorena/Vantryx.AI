package com.vantryx.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vantryx.api.config.JwtService;
import com.vantryx.api.dto.UserDTO;
import com.vantryx.api.dto.UserRegistrationRequest;
import com.vantryx.api.service.UserService;
import com.vantryx.api.model.Role; // IMPORTANTE: Importa tu Enum de Roles
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/users debería devolver todos los usuarios")
    void shouldReturnAllUsers() throws Exception {
        // Usamos el Builder para crear el UserDTO
        UserDTO user = UserDTO.builder()
                .id(1L)
                .username("admin")
                .email("admin@vantryx.com")
                .role(Role.ADMIN) // Usamos el Enum directamente
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.findAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/users/register debería crear un usuario nuevo")
    void shouldRegisterUser() throws Exception {
        // 1. Preparamos la petición (Asegúrate de que UserRegistrationRequest también tenga @Builder)
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("nuevoUsuario")
                .password("Password123!")
                .email("test@vantryx.com")
                .build();

        // 2. Preparamos la respuesta esperada usando el Builder
        UserDTO responseDto = UserDTO.builder()
                .id(1L)
                .username("nuevoUsuario")
                .email("test@vantryx.com")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.register(any(UserRegistrationRequest.class))).thenReturn(responseDto);

        // 3. Ejecución
        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(jsonPath("$.username").value("nuevoUsuario"))
                .andExpect(jsonPath("$.email").value("test@vantryx.com"));
    }
}