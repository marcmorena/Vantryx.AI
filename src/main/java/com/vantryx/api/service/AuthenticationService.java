package com.vantryx.api.service;

import com.vantryx.api.config.JwtService;
import com.vantryx.api.dto.AuthResponse;
import com.vantryx.api.dto.LoginRequest;
import com.vantryx.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse authenticate(LoginRequest request) {
        // 1. Esto valida usuario y contraseña. Si fallan, lanza una excepción automáticamente.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. Si llegamos aquí, el usuario es válido. Lo buscamos para generar el token.
        var userEntity = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        // Creamos un UserDetails "al vuelo" para el JwtService
        var userDetails = User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .roles(userEntity.getRole().name())
                .build();

        // 3. Generamos el token
        var jwtToken = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}