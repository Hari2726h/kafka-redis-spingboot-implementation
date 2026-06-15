package com.example.demo.repository;

import com.example.demo.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  CartItemRepository                                           ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Data access for CartItem entities. Enables looking up a      ║
 * ║  specific item in a cart by cart ID + product ID combination. ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Repository Pattern                           ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find a specific item in a cart by product.
     * Used when updating quantity or checking if a product is already in the cart.
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
