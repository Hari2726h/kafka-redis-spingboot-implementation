package com.example.demo.controller;

import com.example.demo.dto.ShipmentRequest;
import com.example.demo.dto.ShipmentResponse;
import com.example.demo.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  ShipmentController                                           ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  REST endpoints for shipment management and tracking.         ║
 * ║  Used by both admin (create/update) and customers (track).    ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: MVC Controller (thin controller)             ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipments")
@Tag(name = "Shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping("/order/{orderId}")
    @Operation(summary = "Create shipment for an order")
    public ResponseEntity<ShipmentResponse> createShipment(
            @PathVariable Long orderId,
            @Valid @RequestBody ShipmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shipmentService.createShipment(orderId, request));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get shipment by order ID")
    public ResponseEntity<ShipmentResponse> getByOrderId(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(shipmentService.getByOrderId(orderId));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update shipment status")
    public ResponseEntity<ShipmentResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(shipmentService.updateStatus(id, status));
    }

    @GetMapping("/track/{trackingNumber}")
    @Operation(summary = "Track shipment by tracking number")
    public ResponseEntity<ShipmentResponse> trackByTrackingNumber(
            @PathVariable String trackingNumber
    ) {
        return ResponseEntity.ok(shipmentService.trackByTrackingNumber(trackingNumber));
    }
}
