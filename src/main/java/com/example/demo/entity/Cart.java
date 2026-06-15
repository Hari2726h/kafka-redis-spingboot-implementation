package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  Cart Entity                                                  ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Represents a shopping cart. Each user has exactly ONE cart    ║
 * ║  (1:1 relationship). The cart persists between sessions —     ║
 * ║  it's a database-backed cart, not a session-based one.        ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - OneToOne with User — one cart per user                     ║
 * ║  - OneToMany with CartItem — a cart can have many items       ║
 * ║  - CascadeType.ALL — when cart is saved/deleted, items too    ║
 * ║  - orphanRemoval = true — removing an item from the list      ║
 * ║    will DELETE it from the database                           ║
 * ║                                                               ║
 * ║  DESIGN PATTERNS:                                             ║
 * ║  - Aggregate Root (Cart is the root of the Cart aggregate)    ║
 * ║  - Builder Pattern (via Lombok)                               ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-one with User. Each user has exactly one cart.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * All items in this cart.
     * CascadeType.ALL: save/update/delete operations on Cart
     * automatically cascade to CartItem entities.
     * orphanRemoval: if a CartItem is removed from this list,
     * it's automatically deleted from the database.
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

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
