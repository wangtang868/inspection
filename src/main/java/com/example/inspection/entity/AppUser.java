package com.example.inspection.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "app_user")
@Getter @Setter
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false, name = "password_hash")
    private String passwordHash;
    @Column(name = "display_name")
    private String displayName;
    private Integer status = 1;
    @Column(name = "created_at") private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;

    @PrePersist void prePersist() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate  void preUpdate()  { updatedAt = Instant.now(); }
}
