package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  CartResponse DTO                                             ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Returns the full cart view to the client, including all      ║
 * ║  items, their prices, and the cart total.                     ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: DTO + Nested DTO                             ║
 * ║  - CartResponse contains a list of CartItemDto                ║
 * ║  - This avoids exposing JPA entity relationships to the API   ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Data
@Builder
public class CartResponse {

    private Long cartId;
    private Long userId;
    private List<CartItemDto> items;
    private Double totalAmount;

    /**
     * Nested DTO for individual cart items.
     * Flattens the CartItem → Product relationship into a single object.
     */
    @Data
    @Builder
    public static class CartItemDto {
        private Long productId;
        private String productName;
        private Double productPrice;
        private Integer quantity;
        private Double subtotal;
    }

    /**
     * Factory method: builds a CartResponse from a Cart entity.
     */
    public static CartResponse fromEntity(com.example.demo.entity.Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(item -> CartItemDto.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productPrice(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getProduct().getPrice() * item.getQuantity())
                        .build())
                .toList();

        double total = itemDtos.stream()
                .mapToDouble(CartItemDto::getSubtotal)
                .sum();

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .items(itemDtos)
                .totalAmount(total)
                .build();
    }
}
