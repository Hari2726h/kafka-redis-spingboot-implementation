package com.example.demo.enums;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  ShipmentStatus Enum                                          ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Tracks the lifecycle of a shipment as a state machine.       ║
 * ║                                                               ║
 * ║  State transitions:                                           ║
 * ║    PROCESSING → SHIPPED → IN_TRANSIT → DELIVERED              ║
 * ║    Any state  → RETURNED                                      ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: State Pattern (simplified via enum)          ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
public enum ShipmentStatus {
    PROCESSING,
    SHIPPED,
    IN_TRANSIT,
    DELIVERED,
    RETURNED
}
