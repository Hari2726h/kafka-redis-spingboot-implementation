package com.example.demo.entity;
import com.example.demo.enums.ProductStatus;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  Product Entity (UPDATED)                                     ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Represents a product in the e-commerce catalog. This is the  ║
 * ║  ORIGINAL entity from the demo project, now extended with     ║
 * ║  description and category fields for e-commerce use.          ║
 * ║                                                               ║
 * ║  WHAT CHANGED:                                                ║
 * ║  - Added 'description' field — product detail text            ║
 * ║  - Added 'category' field — for filtering/grouping            ║
 * ║  - Implements Serializable — required for Redis serialization ║
 * ║    (Redis needs to serialize/deserialize Product objects)      ║
 * ║                                                               ║
 * ║  DESIGN PATTERNS:                                             ║
 * ║  - Builder Pattern (via Lombok @Builder)                      ║
 * ║  - Active Record (JPA entity = DB row)                        ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(
        name = "products",
        indexes = {
                @Index(
                        name = "idx_product_name",
                        columnList = "name"
                ),
                @Index(
                        name = "idx_product_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    /**
     * NEW: Detailed product description for e-commerce display.
     * Nullable — existing products without descriptions won't break.
     */
    private String description;

    /**
     * NEW: Product category for filtering (e.g., "Electronics", "Clothing").
     * Nullable — existing products without categories won't break.
     */
    private String category;

    private Double price;

        @Column(nullable = false)

    @Enumerated(EnumType.STRING)
    private ProductStatus status;
}
