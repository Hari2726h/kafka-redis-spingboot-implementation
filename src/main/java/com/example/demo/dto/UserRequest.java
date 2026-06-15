package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  UserRequest DTO                                              ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Data Transfer Object for user creation/update requests.      ║
 * ║  Decouples the API contract from the User entity — the       ║
 * ║  client never sees internal fields like id, createdAt, etc.   ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - @Valid in the controller triggers validation               ║
 * ║  - @NotBlank ensures non-null, non-empty strings              ║
 * ║  - @Email validates email format                              ║
 * ║  - @Size enforces password minimum length                     ║
 * ║  - The service maps this DTO to a User entity                 ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: DTO (Data Transfer Object)                   ║
 * ║  - Prevents over-posting (client can't set id, role, etc.)    ║
 * ║  - Validation lives on the DTO, not the entity                ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Data
public class UserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;

    /**
     * Role as a string: "ADMIN" or "CUSTOMER".
     * If null/blank, defaults to CUSTOMER in the service layer.
     */
    private String role;
}
