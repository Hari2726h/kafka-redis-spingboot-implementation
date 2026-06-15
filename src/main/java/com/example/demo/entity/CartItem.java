package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  CartItem Entity                                              ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Represents a single line item in a shopping cart. Links a    ║
 * ║  Product to a Cart with a quantity.                           ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - ManyToOne with Cart — many items belong to one cart        ║
 * ║  - ManyToOne with Product — each item references a product    ║
 * ║  - UniqueConstraint(cart_id, product_id) — prevents duplicate ║
 * ║    entries for the same product in one cart                   ║
 * ║                                                               ║
 * ║  DESIGN PATTERNS:                                             ║
 * ║  - Value Object (represents a quantity of a product in cart)  ║
 * ║  - Builder Pattern (via Lombok)                               ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cart_product",
                        columnNames = {"cart_id", "product_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The cart this item belongs to.
     * FetchType.LAZY: don't load the entire Cart when loading a CartItem.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    /**
     * The product being added to the cart.
     * FetchType.LAZY would be ideal, but we often need product details
     * (name, price) when displaying the cart, so EAGER is acceptable here.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;
}
