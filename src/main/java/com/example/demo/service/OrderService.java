package com.example.demo.service;

import com.example.demo.dto.OrderRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.ActionType;
import com.example.demo.enums.OrderStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.kafka.DomainEventProducer;
import com.example.demo.kafka.HistoryEvent;
import com.example.demo.kafka.HistoryProducer;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  OrderService                                                 ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Orchestrates the order creation process — the most complex   ║
 * ║  business flow in the system. It coordinates between Cart,    ║
 * ║  Inventory, and Order modules.                                ║
 * ║                                                               ║
 * ║  HOW "CREATE ORDER" WORKS (step by step):                    ║
 * ║  1. Validate user exists and has items in cart                ║
 * ║  2. For each cart item, RESERVE stock in inventory            ║
 * ║  3. Create Order with status=PENDING                          ║
 * ║  4. Create OrderItems with snapshot prices                    ║
 * ║  5. Clear the user's cart                                     ║
 * ║  6. Publish order-created event to Kafka                      ║
 * ║  7. Publish audit event to Kafka                              ║
 * ║                                                               ║
 * ║  HOW "CANCEL ORDER" WORKS:                                   ║
 * ║  1. Validate order exists and is cancellable (PENDING/CONFIRM)║
 * ║  2. Release all reserved stock back to inventory              ║
 * ║  3. Update order status to CANCELLED                          ║
 * ║  4. Publish events                                            ║
 * ║                                                               ║
 * ║  DESIGN PATTERNS:                                             ║
 * ║  - Service Layer (orchestration)                              ║
 * ║  - Factory Pattern (order number generation)                  ║
 * ║  - State Machine (order status transitions)                   ║
 * ║  - Observer (Kafka events)                                    ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final InventoryService inventoryService;
    private final CartService cartService;
    private final HistoryProducer historyProducer;
    private final DomainEventProducer domainEventProducer;

    // ─── CREATE ORDER ────────────────────────────────────────────

    /**
     * Creates an order from the user's cart.
     *
     * This is a TRANSACTIONAL operation — if any step fails
     * (e.g., insufficient stock), the entire transaction rolls back.
     * No order is created, no stock is reserved.
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Long userId = request.getUserId();

        // 1. Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 2. Get cart with items
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "No cart found for user " + userId));

        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        // 3. Generate unique order number
        String orderNumber = generateOrderNumber();

        // 4. Create order entity
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(0.0)
                .build();

        // 5. Convert cart items to order items + reserve stock
        double totalAmount = 0.0;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Reserve stock — throws InsufficientStockException if not enough
            inventoryService.reserveStock(product.getId(), cartItem.getQuantity());

            // Create order item with SNAPSHOT price
            double subtotal = product.getPrice() * cartItem.getQuantity();
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())  // Snapshot price
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(orderItem);
            totalAmount += subtotal;
        }

        order.setTotalAmount(totalAmount);

        // 6. Save order
        Order savedOrder = orderRepository.save(order);

        // 7. Clear the cart
        cartService.clearCart(userId);

        // 8. Publish events
        publishHistory(savedOrder, ActionType.CREATE);
        publishDomainEvent(savedOrder, "ORDER_CREATED");

        log.info("Created order {} for user {} — total: {}",
                orderNumber, userId, totalAmount);
        return OrderResponse.fromEntity(savedOrder);
    }

    // ─── READ ────────────────────────────────────────────────────

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return OrderResponse.fromEntity(order);
    }

    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    // ─── CANCEL ORDER ────────────────────────────────────────────

    /**
     * Cancels an order and releases all reserved stock.
     *
     * Only PENDING and CONFIRMED orders can be cancelled.
     * SHIPPED/DELIVERED orders cannot be cancelled.
     */
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        // Validate state transition
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot cancel order in status: " + order.getStatus());
        }

        // Release reserved stock for each item
        for (OrderItem item : order.getItems()) {
            inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity());
        }

        // Update status
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        // Publish events
        publishHistory(savedOrder, ActionType.UPDATE);
        publishDomainEvent(savedOrder, "ORDER_CANCELLED");

        log.info("Cancelled order {}", order.getOrderNumber());
        return OrderResponse.fromEntity(savedOrder);
    }

    // ─── STATUS UPDATE (internal) ────────────────────────────────

    /**
     * Updates order status. Used internally by PaymentService and
     * ShipmentService to advance the order lifecycle.
     */
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        orderRepository.save(order);

        publishHistory(order, ActionType.UPDATE);
        log.info("Order {} status: {} → {}", order.getOrderNumber(), oldStatus, newStatus);
    }

    // ─── Helpers ─────────────────────────────────────────────────

    /**
     * FACTORY PATTERN: Generates a unique, human-readable order number.
     * Format: ORD-YYYYMMDD-XXXXXX (6 random hex chars)
     *
     * Example: ORD-20260615-A3F2C1
     */
    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + date + "-" + random;
    }

    private void publishHistory(Order order, ActionType actionType) {
        historyProducer.sendEvent(new HistoryEvent(
                Order.class.getSimpleName(),
                order.getId(),
                actionType.name()
        ));
    }

    private void publishDomainEvent(Order order, String eventType) {
        String json = String.format(
                "{\"type\":\"%s\",\"orderId\":%d,\"orderNumber\":\"%s\",\"userId\":%d,\"totalAmount\":%.2f,\"status\":\"%s\",\"timestamp\":\"%s\"}",
                eventType,
                order.getId(),
                order.getOrderNumber(),
                order.getUser().getId(),
                order.getTotalAmount(),
                order.getStatus(),
                LocalDateTime.now()
        );
        domainEventProducer.publish("order-events", json);
    }
}
