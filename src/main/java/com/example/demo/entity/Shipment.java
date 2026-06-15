package com.example.demo.entity;

import com.example.demo.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  Shipment Entity                                              ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Tracks the delivery lifecycle of an order. Each order has    ║
 * ║  at most one shipment (1:1). Created after payment succeeds.  ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - trackingNumber: unique, human-readable ID (e.g.,          ║
 * ║    "SHIP-20260615-DEF456")                                   ║
 * ║  - ShipmentStatus: PROCESSING → SHIPPED → IN_TRANSIT →       ║
 * ║    DELIVERED (state machine)                                  ║
 * ║  - carrier: shipping company name (e.g., "FedEx", "DHL")     ║
 * ║  - estimatedDelivery: when the package should arrive          ║
 * ║  - actualDelivery: when it actually arrived (null until       ║
 * ║    status = DELIVERED)                                        ║
 * ║                                                               ║
 * ║  DESIGN PATTERNS:                                             ║
 * ║  - State Machine (ShipmentStatus transitions)                 ║
 * ║  - Factory Pattern (tracking number generation)               ║
 * ║  - Builder Pattern (via Lombok)                               ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(
        name = "shipments",
        indexes = {
                @Index(name = "idx_shipment_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String trackingNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    private String carrier;

    private LocalDateTime estimatedDelivery;

    /**
     * Null until the shipment is actually delivered.
     */
    private LocalDateTime actualDelivery;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
