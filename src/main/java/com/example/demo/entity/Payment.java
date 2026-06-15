package com.example.demo.entity;

import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  Payment Entity                                               ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Records payment transactions for orders. Each order has      ║
 * ║  exactly one payment record (1:1). In this project, payment   ║
 * ║  processing is SIMULATED — no real gateway integration.       ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - transactionId: unique ID generated to simulate a gateway   ║
 * ║    response (e.g., "TXN-20260615-XYZ789")                    ║
 * ║  - PaymentStatus: PENDING → COMPLETED or FAILED              ║
 * ║  - paidAt: timestamp when payment was successfully processed  ║
 * ║    (null if PENDING or FAILED)                                ║
 * ║                                                               ║
 * ║  DESIGN PATTERNS:                                             ║
 * ║  - Strategy Pattern (method field could route to different    ║
 * ║    payment processors in a real system)                       ║
 * ║  - Builder Pattern (via Lombok)                               ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    /**
     * Timestamp when payment was successfully completed.
     * Null if payment is still PENDING or has FAILED.
     */
    private LocalDateTime paidAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
