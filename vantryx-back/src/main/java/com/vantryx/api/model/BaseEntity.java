package com.vantryx.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass // Indica que esta clase no es una entidad, pero sus campos sí se mapearán en las hijas
public abstract class BaseEntity {

    @Id // <--- AGREGAR ESTO
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <--- AGREGAR ESTO
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private boolean isDeleted = false; // Borrado lógico, fundamental en ERPs

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
