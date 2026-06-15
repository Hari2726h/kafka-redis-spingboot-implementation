package com.example.demo.enums;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  PaymentMethod Enum                                           ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Defines the payment methods available in the system.         ║
 * ║  In a real system, each method would route to a different     ║
 * ║  payment gateway adapter (Strategy Pattern).                  ║
 * ║                                                               ║
 * ║  For this project, payment is simulated — the method is       ║
 * ║  stored for record-keeping but doesn't affect processing.     ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Type-Safe Enum + Strategy (future)           ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
public enum PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    UPI,
    NET_BANKING
}
