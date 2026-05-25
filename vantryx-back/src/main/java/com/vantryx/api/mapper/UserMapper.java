package com.vantryx.api.mapper;

import com.vantryx.api.dto.UserDTO;
import com.vantryx.api.dto.UserRegistrationRequest;
import com.vantryx.api.model.Role;
import com.vantryx.api.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        if (user == null) return null;

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public User toEntity(UserRegistrationRequest request) {
        if (request == null) return null;

        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword()) // ¡Ojo! Luego la encriptaremos
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .active(true)
                .build();
    }
}
