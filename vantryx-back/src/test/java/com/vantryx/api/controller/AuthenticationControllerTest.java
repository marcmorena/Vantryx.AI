package com.vantryx.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vantryx.api.config.JwtService;
import com.vantryx.api.dto.AuthResponse;
import com.vantryx.api.dto.LoginRequest;
import com.vantryx.api.service.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authService;

    @MockBean
    private JwtService jwtService; // Necesario para cargar el contexto de seguridad

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/login debería devolver un token JWT")
    void shouldLoginSuccessfully() throws Exception {
        // 1. Preparamos el Request (Uso Setters por si no tienes @Builder)
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password123");

        // 2. Preparamos el Response (Uso Builder si tu AuthResponse lo tiene)
        // Si no tiene builder, cámbialo por: AuthResponse response = new AuthResponse("mock-jwt-token");
        AuthResponse response = AuthResponse.builder()
                .token("eyJhbGciOiJIUzI1NiJ9.mocktoken")
                .build();

        when(authService.authenticate(any(LoginRequest.class))).thenReturn(response);

        // 3. Ejecución
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf()) // Prevenimos 403 por falta de token CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiJ9.mocktoken"));
    }
}