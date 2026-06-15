package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  OrderItem Entity                                             ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Represents a single line item in an order. It's a SNAPSHOT   ║
 * ║  of the product at the time of purchase — storing unitPrice   ║
 * ║  and subtotal independently of the current product price.     ║
 * ║                                                               ║
 * ║  WHY SNAPSHOT PRICES?                                         ║
 * ║  Product prices can change after an order is placed. If we    ║
 * ║  only stored product_id, the order total would change when    ║
 * ║  the product price changes. That's incorrect — the customer   ║
 * ║  was charged based on the price at checkout time.             ║
 * ║                                                               ║
 * ║  DESIGN PATTERNS:                                             ║
 * ║  - Value Object (immutable snapshot of purchase data)         ║
 * ║  - Builder Pattern (via Lombok)                               ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    /**
     * Price per unit AT THE TIME OF PURCHASE.
     * This is a snapshot — it won't change even if the product price changes later.
     */
    @Column(nullable = false)
    private Double unitPrice;

    /**
     * quantity × unitPrice, pre-calculated for performance.
     */
    @Column(nullable = false)
    private Double subtotal;
}
