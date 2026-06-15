package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  OrderRequest DTO                                             ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Input payload for creating an order. Only requires the user  ║
 * ║  ID — the items come from the user's cart automatically.      ║
 * ║                                                               ║
 * ║  WHY SO SIMPLE?                                               ║
 * ║  In this design, "place order" means "convert my entire cart  ║
 * ║  into an order". The cart already has all items and quantities.║
 * ║  This is the Amazon/Flipkart model.                           ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: DTO                                          ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Data
public class OrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;
}
