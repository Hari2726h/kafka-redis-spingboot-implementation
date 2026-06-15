package com.example.demo.controller;

import com.example.demo.dto.OrderRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  OrderController                                              ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  REST endpoints for order lifecycle management. Handles       ║
 * ║  order creation (checkout), viewing, and cancellation.        ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: MVC Controller (thin controller)             ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Tag(name = "Orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create order from user's cart")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all orders for a user")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
