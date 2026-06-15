package com.example.demo.dto;

import com.example.demo.enums.ShipmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  ShipmentResponse DTO                                         ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Returns shipment tracking information to the client.         ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: DTO                                          ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Data
@Builder
public class ShipmentResponse {

    private Long id;
    private String trackingNumber;
    private Long orderId;
    private String orderNumber;
    private ShipmentStatus status;
    private String carrier;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ShipmentResponse fromEntity(com.example.demo.entity.Shipment shipment) {
        return ShipmentResponse.builder()
                .id(shipment.getId())
                .trackingNumber(shipment.getTrackingNumber())
                .orderId(shipment.getOrder().getId())
                .orderNumber(shipment.getOrder().getOrderNumber())
                .status(shipment.getStatus())
                .carrier(shipment.getCarrier())
                .estimatedDelivery(shipment.getEstimatedDelivery())
                .actualDelivery(shipment.getActualDelivery())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .build();
    }
}
