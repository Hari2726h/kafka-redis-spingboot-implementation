package com.example.demo.controller;

import com.example.demo.dto.CartItemRequest;
import com.example.demo.dto.CartResponse;
import com.example.demo.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  CartController                                               ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  REST endpoints for shopping cart operations. Customers use   ║
 * ║  these to build their order before checkout.                  ║
 * ║                                                               ║
 * ║  URL DESIGN:                                                  ║
 * ║  All endpoints are scoped to a user: /api/cart/{userId}/...   ║
 * ║  In a real app with auth, userId would come from the JWT      ║
 * ║  token, not the URL. Here we use URL params for simplicity.   ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: MVC Controller (thin controller)             ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
@Tag(name = "Cart")
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    @Operation(summary = "View cart for a user")
    public ResponseEntity<CartResponse> getCart(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/{userId}/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable Long userId,
            @Valid @RequestBody CartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @PutMapping("/{userId}/items/{productId}")
    @Operation(summary = "Update item quantity in cart")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestParam int quantity
    ) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, productId, quantity));
    }

    @DeleteMapping("/{userId}/items/{productId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Long userId,
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Clear entire cart")
    public ResponseEntity<Void> clearCart(
            @PathVariable Long userId
    ) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
