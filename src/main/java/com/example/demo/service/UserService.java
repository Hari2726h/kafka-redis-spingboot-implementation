package com.example.demo.service;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.enums.ActionType;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.kafka.HistoryEvent;
import com.example.demo.kafka.HistoryProducer;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  UserService                                                  ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Contains ALL business logic for user management. The         ║
 * ║  controller is "thin" — it only handles HTTP concerns.        ║
 * ║  This service handles:                                        ║
 * ║  - User registration with password hashing                   ║
 * ║  - CRUD operations                                            ║
 * ║  - Audit trail via Kafka HistoryProducer                      ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  1. Request DTO comes in from the controller                  ║
 * ║  2. Service validates business rules (e.g., unique email)     ║
 * ║  3. Maps DTO to Entity, performs the operation                ║
 * ║  4. Publishes a history event to Kafka for audit              ║
 * ║  5. Returns a Response DTO (never the raw entity)             ║
 * ║                                                               ║
 * ║  DESIGN PATTERNS:                                             ║
 * ║  - Service Layer Pattern (business logic encapsulation)       ║
 * ║  - DTO Pattern (input/output decoupling)                      ║
 * ║  - Observer Pattern (audit via Kafka events)                  ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final HistoryProducer historyProducer;

    /**
     * BCryptPasswordEncoder is thread-safe, so a single instance is fine.
     * BCrypt automatically generates a random salt per password.
     *
     * WHY BCrypt?
     * - It's intentionally slow (configurable work factor)
     * - Each hash includes a unique salt (no rainbow table attacks)
     * - Industry standard for password storage
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ─── CREATE ──────────────────────────────────────────────────

    @Transactional
    public UserResponse createUser(UserRequest request) {
        // Business rule: no duplicate emails
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email already registered: " + request.getEmail());
        }

        // Determine role: default to CUSTOMER if not specified
        UserRole role = UserRole.CUSTOMER;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                role = UserRole.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid role: " + request.getRole() + ". Must be ADMIN or CUSTOMER");
            }
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))  // Hash the password
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(role)
                .build();

        User savedUser = userRepository.save(user);
        publishHistory(savedUser, ActionType.CREATE);
        log.info("Created user {} with role {}", savedUser.getId(), savedUser.getRole());

        return UserResponse.fromEntity(savedUser);
    }

    // ─── READ ────────────────────────────────────────────────────

    public UserResponse getUserById(Long id) {
        User user = findUserOrThrow(id);
        return UserResponse.fromEntity(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    // ─── UPDATE ──────────────────────────────────────────────────

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = findUserOrThrow(id);

        // Only update fields that are provided
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User savedUser = userRepository.save(user);
        publishHistory(savedUser, ActionType.UPDATE);
        log.info("Updated user {}", savedUser.getId());

        return UserResponse.fromEntity(savedUser);
    }

    // ─── DELETE ──────────────────────────────────────────────────

    @Transactional
    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        userRepository.delete(user);
        publishHistory(user, ActionType.DELETE);
        log.info("Deleted user {}", id);
    }

    // ─── Helpers ─────────────────────────────────────────────────

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Publishes an audit event to Kafka — same pattern as ProductService.
     * The HistoryConsumer will receive this and store it in the history table.
     */
    private void publishHistory(User user, ActionType actionType) {
        historyProducer.sendEvent(new HistoryEvent(
                User.class.getSimpleName(),
                user.getId(),
                actionType.name()
        ));
    }
}
