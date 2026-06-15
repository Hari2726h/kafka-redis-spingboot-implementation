package com.example.demo.enums;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  PaymentStatus Enum                                           ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Tracks the state of a payment transaction.                   ║
 * ║                                                               ║
 * ║  PENDING   — Payment initiated but not yet processed          ║
 * ║  COMPLETED — Payment successfully processed                   ║
 * ║  FAILED    — Payment processing failed                        ║
 * ║  REFUNDED  — Payment was refunded (e.g. order cancelled)      ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Type-Safe Enum                               ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}
