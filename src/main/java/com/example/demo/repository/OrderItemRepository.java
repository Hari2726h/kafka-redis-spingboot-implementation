package com.example.demo.repository;

import com.example.demo.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  OrderItemRepository                                          ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Data access for OrderItem entities. Mostly used via cascade  ║
 * ║  from Order, but exists for direct queries if needed.         ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Repository Pattern                           ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // OrderItems are typically managed through Order's cascade.
    // Direct queries can be added here if needed for reporting.
}
