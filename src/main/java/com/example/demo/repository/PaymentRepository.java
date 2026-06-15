package com.example.demo.repository;

import com.example.demo.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  PaymentRepository                                            ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Data access for Payment entities. Primary lookups are by     ║
 * ║  order ID and transaction ID.                                 ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Repository Pattern                           ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find the payment record for a specific order.
     * Each order has at most one payment (1:1).
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * Find a payment by its transaction ID.
     * Useful for payment status lookups and reconciliation.
     */
    Optional<Payment> findByTransactionId(String transactionId);
}
