package com.example.demo.exception;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  ResourceNotFoundException                                    ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Thrown when a database lookup finds no matching record.       ║
 * ║  Replaces scattered ResponseStatusException(NOT_FOUND) calls  ║
 * ║  with a single, semantic exception that the                   ║
 * ║  GlobalExceptionHandler maps to HTTP 404.                     ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Custom Unchecked Exception                   ║
 * ║  - Extends RuntimeException so callers don't need try/catch   ║
 * ║  - Works with @ControllerAdvice for centralized handling      ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * @param resourceName  e.g. "User", "Order", "Product"
     * @param fieldName     e.g. "id", "email", "orderNumber"
     * @param fieldValue    the actual value that was not found
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
