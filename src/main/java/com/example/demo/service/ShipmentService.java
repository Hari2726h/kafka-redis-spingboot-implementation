package com.example.demo.service;

import com.example.demo.dto.ShipmentRequest;
import com.example.demo.dto.ShipmentResponse;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Shipment;
import com.example.demo.enums.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.kafka.DomainEventProducer;
import com.example.demo.kafka.HistoryEvent;
import com.example.demo.kafka.HistoryProducer;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ShipmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  ShipmentService                                              ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Manages the delivery lifecycle of orders. A shipment is      ║
 * ║  created after payment is confirmed, and tracks the package   ║
 * ║  from warehouse to customer.                                  ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - createShipment: generates tracking number, sets status     ║
 * ║    to PROCESSING, assigns carrier, estimates delivery date    ║
 * ║  - updateStatus: advances the shipment through its lifecycle  ║
 * ║    (PROCESSING → SHIPPED → IN_TRANSIT → DELIVERED)            ║
 * ║  - When status becomes DELIVERED:                             ║
 * ║    1. Sets actualDelivery timestamp                           ║
 * ║    2. Updates order status to DELIVERED                       ║
 * ║    3. Confirms reserved stock (deducts from inventory)        ║
 * ║                                                               ║
 * ║  DESIGN PATTERNS:                                             ║
 * ║  - State Machine (ShipmentStatus transitions)                 ║
 * ║  - Factory Pattern (tracking number generation)               ║
 * ║  - Observer (Kafka events)                                    ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final HistoryProducer historyProducer;
    private final DomainEventProducer domainEventProducer;

    // ─── CREATE SHIPMENT ─────────────────────────────────────────

    /**
     * Creates a shipment for an order. The order must be CONFIRMED
     * (payment successful) before shipping.
     */
    @Transactional
    public ShipmentResponse createShipment(Long orderId, ShipmentRequest request) {
        // 1. Validate order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order must be CONFIRMED before shipping. Current status: " + order.getStatus());
        }

        // 2. Check for existing shipment
        if (shipmentRepository.findByOrderId(orderId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Shipment already exists for order " + orderId);
        }

        // 3. Create shipment
        String trackingNumber = generateTrackingNumber();

        Shipment shipment = Shipment.builder()
                .trackingNumber(trackingNumber)
                .order(order)
                .status(ShipmentStatus.PROCESSING)
                .carrier(request.getCarrier())
                .estimatedDelivery(LocalDateTime.now().plusDays(5))  // 5-day estimate
                .build();

        Shipment saved = shipmentRepository.save(shipment);

        // 4. Update order status to SHIPPED
        orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED);

        // 5. Publish events
        publishHistory(saved, ActionType.CREATE);
        publishDomainEvent(saved, "SHIPMENT_CREATED");

        log.info("Created shipment {} for order {}", trackingNumber, order.getOrderNumber());
        return ShipmentResponse.fromEntity(saved);
    }

    // ─── UPDATE STATUS ───────────────────────────────────────────

    /**
     * Updates the shipment status. When status becomes DELIVERED,
     * the order is also updated and inventory is confirmed.
     */
    @Transactional
    public ShipmentResponse updateStatus(Long shipmentId, String newStatusStr) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));

        ShipmentStatus newStatus;
        try {
            newStatus = ShipmentStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status: " + newStatusStr
                            + ". Valid values: PROCESSING, SHIPPED, IN_TRANSIT, DELIVERED, RETURNED");
        }

        ShipmentStatus oldStatus = shipment.getStatus();
        shipment.setStatus(newStatus);

        // Handle DELIVERED: set actual delivery time + update order + confirm stock
        if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setActualDelivery(LocalDateTime.now());
            orderService.updateOrderStatus(shipment.getOrder().getId(), OrderStatus.DELIVERED);

            // Confirm stock (deduct from inventory)
            Order order = shipment.getOrder();
            for (OrderItem item : order.getItems()) {
                inventoryService.confirmReservedStock(item.getProduct().getId(), item.getQuantity());
            }
        }

        Shipment saved = shipmentRepository.save(shipment);

        publishHistory(saved, ActionType.UPDATE);
        publishDomainEvent(saved, "SHIPMENT_STATUS_UPDATED");

        log.info("Shipment {} status: {} → {}", shipment.getTrackingNumber(), oldStatus, newStatus);
        return ShipmentResponse.fromEntity(saved);
    }

    // ─── READ ────────────────────────────────────────────────────

    public ShipmentResponse getByOrderId(Long orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "orderId", orderId));
        return ShipmentResponse.fromEntity(shipment);
    }

    public ShipmentResponse trackByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shipment", "trackingNumber", trackingNumber));
        return ShipmentResponse.fromEntity(shipment);
    }

    // ─── Helpers ─────────────────────────────────────────────────

    /**
     * FACTORY PATTERN: Generates a unique tracking number.
     * Format: SHIP-YYYYMMDD-XXXXXXXX (8 random hex chars)
     */
    private String generateTrackingNumber() {
        String date = LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "SHIP-" + date + "-" + random;
    }

    private void publishHistory(Shipment shipment, ActionType actionType) {
        historyProducer.sendEvent(new HistoryEvent(
                Shipment.class.getSimpleName(),
                shipment.getId(),
                actionType.name()
        ));
    }

    private void publishDomainEvent(Shipment shipment, String eventType) {
        String json = String.format(
                "{\"type\":\"%s\",\"shipmentId\":%d,\"trackingNumber\":\"%s\",\"orderId\":%d,\"status\":\"%s\",\"carrier\":\"%s\",\"timestamp\":\"%s\"}",
                eventType,
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getOrder().getId(),
                shipment.getStatus(),
                shipment.getCarrier(),
                LocalDateTime.now()
        );
        domainEventProducer.publish("shipment-events", json);
    }
}
