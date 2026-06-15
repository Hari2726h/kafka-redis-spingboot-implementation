package com.example.demo.enums;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  UserRole Enum                                                ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Defines the two roles a user can have in the system.         ║
 * ║  Stored as a STRING in the database (via @Enumerated).        ║
 * ║                                                               ║
 * ║  ADMIN   — Can manage products, inventory, view all orders    ║
 * ║  CUSTOMER — Can browse, add to cart, place orders             ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Type-Safe Enum                               ║
 * ║  - Prevents invalid role strings like "SUPERADMIN"            ║
 * ║  - Stored as EnumType.STRING for readability in the DB        ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
public enum UserRole {
    ADMIN,
    CUSTOMER
}
