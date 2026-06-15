package com.example.demo.dto;

import com.example.demo.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  UserResponse DTO                                             ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Controls what user data is sent back to the client. The      ║
 * ║  password field is NEVER included in the response.            ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - Service creates this from a User entity                    ║
 * ║  - Controller returns this, not the User entity               ║
 * ║  - Password is excluded for security                          ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: DTO (Data Transfer Object)                   ║
 * ║  - Security: never expose password hashes over the wire       ║
 * ║  - Decoupling: API shape can evolve independently of entity   ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Data
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method: converts a User entity to a UserResponse DTO.
     * This is a static factory method — an alternative to using a
     * separate mapper class (like MapStruct).
     */
    public static UserResponse fromEntity(com.example.demo.entity.User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
