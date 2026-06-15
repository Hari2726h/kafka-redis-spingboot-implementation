package com.example.demo.dto;

import com.example.demo.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  OrderResponse DTO                                            ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Returns order details to the client, including all items     ║
 * ║  with snapshot prices and the order status.                   ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: DTO + Nested DTO                             ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Data
@Builder
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private Long userId;
    private Double totalAmount;
    private OrderStatus status;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class OrderItemDto {
        private Long productId;
        private String productName;
        private Integer quantity;
        private Double unitPrice;
        private Double subtotal;
    }

    public static OrderResponse fromEntity(com.example.demo.entity.Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> OrderItemDto.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
