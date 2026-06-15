package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  Inventory Entity                                             ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Tracks stock quantity for each product. Separated from the   ║
 * ║  Product entity because inventory changes frequently (every   ║
 * ║  order, every restock) while product metadata changes rarely. ║
 * ║  This separation avoids lock contention on the products table.║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - OneToOne with Product (each product has exactly one        ║
 * ║    inventory record)                                          ║
 * ║  - quantity: total physical stock on hand                     ║
 * ║  - reservedQuantity: stock reserved by pending orders         ║
 * ║    (not yet shipped)                                          ║
 * ║  - Available stock = quantity - reservedQuantity              ║
 * ║                                                               ║
 * ║  DESIGN PATTERNS:                                             ║
 * ║  - Separation of Concerns (inventory ≠ product metadata)     ║
 * ║  - Builder Pattern (via Lombok)                               ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(
        name = "inventory",
        indexes = {
                @Index(name = "idx_inventory_product", columnList = "product_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-one relationship with Product.
     * unique = true ensures each product has at most one inventory record.
     * JoinColumn tells JPA to create a "product_id" foreign key column.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    /**
     * Total physical stock available in the warehouse.
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Stock reserved by pending orders (confirmed but not shipped).
     * This prevents overselling: available = quantity - reservedQuantity.
     */
    @Column(nullable = false)
    private Integer reservedQuantity;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
        if (this.reservedQuantity == null) {
            this.reservedQuantity = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method: calculates available stock.
     * Available = total stock minus reserved stock.
     */
    public int getAvailableQuantity() {
        return this.quantity - this.reservedQuantity;
    }
}
