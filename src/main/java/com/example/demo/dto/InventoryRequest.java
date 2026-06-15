package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  InventoryRequest DTO                                         ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Input payload for inventory operations (initialize stock,    ║
 * ║  add stock, deduct stock).                                    ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: DTO                                          ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Data
public class InventoryRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity;
}
