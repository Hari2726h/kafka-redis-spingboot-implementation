package com.example.demo.enums;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  OrderStatus Enum                                             ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Represents the lifecycle of an order as a state machine.     ║
 * ║                                                               ║
 * ║  State transitions:                                           ║
 * ║    PENDING → CONFIRMED → SHIPPED → DELIVERED                  ║
 * ║    PENDING → CANCELLED                                        ║
 * ║    CONFIRMED → CANCELLED (with stock restoration)             ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: State Pattern (simplified via enum)          ║
 * ║  - Each value represents a distinct state in the order FSM    ║
 * ║  - OrderService enforces valid transitions                    ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
