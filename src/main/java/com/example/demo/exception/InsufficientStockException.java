package com.example.demo.exception;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  InsufficientStockException                                   ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Thrown when an inventory operation would result in negative   ║
 * ║  stock. This is a BUSINESS RULE violation, not a generic       ║
 * ║  error — hence a dedicated exception class.                   ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  InventoryService checks: if (available < requested) throw.   ║
 * ║  GlobalExceptionHandler catches it → HTTP 409 CONFLICT.       ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Domain Exception                             ║
 * ║  - Encodes a specific business rule in the type system         ║
 * ║  - The exception handler knows to return 409 (not 500)        ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long productId, int requested, int available) {
        super(String.format(
                "Insufficient stock for product %d: requested=%d, available=%d",
                productId, requested, available
        ));
    }
}
