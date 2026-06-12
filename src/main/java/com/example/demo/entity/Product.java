package com.example.demo.entity;
import com.example.demo.enums.ProductStatus;

import jakarta.persistence.*;
import lombok.*;

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
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Double price;

        @Column(nullable = false)

    @Enumerated(EnumType.STRING)
    private ProductStatus status;
}
