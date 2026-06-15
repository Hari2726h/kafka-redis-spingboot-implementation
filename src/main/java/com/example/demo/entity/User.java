package com.example.demo.entity;

import com.example.demo.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  User Entity                                                  ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Represents a registered user (customer or admin) in the      ║
 * ║  e-commerce system. Every Cart, Order, and Payment belongs    ║
 * ║  to a User.                                                   ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - @Entity tells JPA this class maps to a database table      ║
 * ║  - @Table(name="users") — "user" is a reserved word in MySQL  ║
 * ║  - @Id + @GeneratedValue — auto-increment primary key         ║
 * ║  - @Column(unique=true) on email — enforces uniqueness at DB  ║
 * ║  - @Enumerated(EnumType.STRING) — stores "ADMIN"/"CUSTOMER"   ║
 * ║    as readable strings, not ordinal integers                  ║
 * ║  - @PrePersist / @PreUpdate — auto-set timestamp fields       ║
 * ║                                                               ║
 * ║  DESIGN PATTERNS:                                             ║
 * ║  - Active Record (JPA entity = DB row)                        ║
 * ║  - Builder Pattern (via Lombok @Builder)                      ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_role", columnList = "role")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback — runs BEFORE the first INSERT.
     * Sets createdAt and updatedAt to "now" so we don't have to
     * remember to set them manually in service code.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback — runs BEFORE every UPDATE.
     * Keeps updatedAt always current.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
