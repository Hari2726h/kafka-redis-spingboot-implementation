package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  UserRepository                                               ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Provides data access methods for User entities. Spring Data  ║
 * ║  JPA auto-generates the implementation at runtime — you only  ║
 * ║  define the interface.                                        ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - Extends JpaRepository<User, Long>                          ║
 * ║    → User is the entity type, Long is the primary key type    ║
 * ║  - JpaRepository provides: save(), findById(), findAll(),     ║
 * ║    delete(), count(), existsById() — all for free             ║
 * ║  - Custom methods use Spring Data query derivation:           ║
 * ║    findByEmail → SELECT * FROM users WHERE email = ?          ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Repository Pattern                           ║
 * ║  - Abstracts the data layer from business logic               ║
 * ║  - Service layer calls repository methods, never writes SQL   ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     * Used for login and duplicate-email checks during registration.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if an email is already taken.
     * More efficient than findByEmail() when you don't need the User object.
     */
    boolean existsByEmail(String email);

    /**
     * Find all users with a specific role.
     * Example: Find all ADMIN users for management dashboards.
     */
    List<User> findByRole(UserRole role);
}
