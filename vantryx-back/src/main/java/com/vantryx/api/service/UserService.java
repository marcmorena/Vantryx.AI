package com.vantryx.api.service;

import com.vantryx.api.dto.UserDTO;
import com.vantryx.api.dto.UserRegistrationRequest;
import com.vantryx.api.exception.UserAlreadyExistsException;
import com.vantryx.api.mapper.UserMapper;
import com.vantryx.api.model.User;
import com.vantryx.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Crea el constructor para la Inyección de Dependencias automáticamente
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO register(UserRegistrationRequest request) {
        // 1. VALIDACIÓN: Comprobamos si el usuario ya existe
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("El nombre de usuario '" + request.getUsername() + "' ya está en uso");
        }

        // 2. VALIDACIÓN: Comprobamos si el email ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("El email '" + request.getEmail() + "' ya está registrado");
        }

        // 3. MAPEO: Convertimos el DTO (JSON) a Entidad (Base de datos)
        User user = userMapper.toEntity(request);

        // 4. SEGURIDAD: Encriptamos la contraseña antes de guardar
        // Nunca guardamos la contraseña en texto plano
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 5. PERSISTENCIA: Guardamos en la base de datos
        User savedUser = userRepository.save(user);

        // 6. RETORNO: Devolvemos el DTO limpio (sin contraseña)
        return userMapper.toDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> findAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return userMapper.toDTO(user);
    }

}
