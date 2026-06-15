package com.example.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  GlobalExceptionHandler                                       ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Centralized error handling for the ENTIRE application.       ║
 * ║  Without this, each controller would need its own try/catch   ║
 * ║  blocks, leading to duplicated error-formatting logic.        ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  Spring's @RestControllerAdvice intercepts exceptions thrown   ║
 * ║  by ANY controller. Each @ExceptionHandler method handles a   ║
 * ║  specific exception type and returns a structured JSON error. ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Controller Advice (AOP-based)                ║
 * ║  - Cross-cutting concern applied globally via AOP proxies     ║
 * ║  - Single Responsibility: error formatting lives in one place ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles 404 Not Found — when a resource doesn't exist in the database.
     * Example: GET /api/users/999 when user 999 doesn't exist.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles 409 Conflict — when a business rule is violated.
     * Example: Trying to order 100 units when only 5 are in stock.
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        log.warn("Insufficient stock: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Handles 400 Bad Request — when @Valid validation fails on a DTO.
     * Example: POST /api/users with an empty email field.
     * Returns a map of field → error message for client-side display.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Validation failed: {}", fieldErrors);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles existing ResponseStatusException from the old ProductService.
     * This ensures backward compatibility with the existing code.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        log.warn("Response status exception: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
    }

    /**
     * Catch-all handler for any unexpected exceptions.
     * Logs the full stack trace and returns a generic 500 error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    // ─── Helper ──────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
