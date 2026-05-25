package com.vantryx.api.dto;

import com.vantryx.api.model.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data // @Data incluye @Getter, @Setter, @ToString...
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
}
