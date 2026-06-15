package com.example.demo.repository;

import com.example.demo.entity.Order;
import com.example.demo.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  OrderRepository                                              ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Data access for Order entities. Supports lookups by user,    ║
 * ║  status, and order number.                                    ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Repository Pattern                           ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders placed by a specific user.
     * Ordered by creation date descending (newest first).
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find an order by its human-readable order number.
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find all orders with a specific status.
     * Useful for admin dashboards and batch processing.
     */
    List<Order> findByStatus(OrderStatus status);
}
