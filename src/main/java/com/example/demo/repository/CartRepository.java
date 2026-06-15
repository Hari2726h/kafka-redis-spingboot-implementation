package com.example.demo.repository;

import com.example.demo.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  CartRepository                                               ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Data access for Cart entities. Since each user has exactly   ║
 * ║  one cart, the primary lookup is by user ID.                  ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Repository Pattern                           ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find a user's cart. Returns Optional because the cart
     * might not exist yet (created on first add-to-cart).
     */
    Optional<Cart> findByUserId(Long userId);
}
