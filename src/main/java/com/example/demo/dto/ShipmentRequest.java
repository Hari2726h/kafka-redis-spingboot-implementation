package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  ShipmentRequest DTO                                          ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Input payload for creating/updating shipments.               ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: DTO                                          ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Data
public class ShipmentRequest {

    @NotBlank(message = "Carrier is required")
    private String carrier;

    /**
     * New status for status update endpoint.
     * Valid values: PROCESSING, SHIPPED, IN_TRANSIT, DELIVERED, RETURNED
     */
    private String status;
}
