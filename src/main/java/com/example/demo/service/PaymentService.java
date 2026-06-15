package com.example.demo.service;

import com.example.demo.dto.PaymentRequest;
import com.example.demo.dto.PaymentResponse;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.enums.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.kafka.DomainEventProducer;
import com.example.demo.kafka.HistoryEvent;
import com.example.demo.kafka.HistoryProducer;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  PaymentService                                               ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Handles payment processing for orders. In this project,     ║
 * ║  payment is SIMULATED — no real gateway integration.          ║
 * ║                                                               ║
 * ║  HOW THE SIMULATION WORKS:                                   ║
 * ║  1. User submits payment for an order                         ║
 * ║  2. System generates a transaction ID                         ║
 * ║  3. Random number determines success (90%) or failure (10%)   ║
 * ║  4. On SUCCESS: order status → CONFIRMED                     ║
 * ║  5. On FAILURE: payment status → FAILED (order stays PENDING) ║
 * ║                                                               ║
 * ║  WHY SIMULATE?                                                ║
 * ║  Real payment gateways (Stripe, Razorpay) require API keys,  ║
 * ║  merchant accounts, and PCI compliance. Simulation lets you   ║
 * ║  test the full order flow without external dependencies.      ║
 * ║                                                               ║
 * ║  DESIGN PATTERNS:                                             ║
 * ║  - Strategy Pattern (PaymentMethod could route to different   ║
 * ║    processors in production)                                  ║
 * ║  - Service Layer Pattern                                      ║
 * ║  - Observer (Kafka events)                                    ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final HistoryProducer historyProducer;
    private final DomainEventProducer domainEventProducer;

    /**
     * Random number generator for simulating payment success/failure.
     * In production, this would be replaced by a real gateway call.
     */
    private final Random random = new Random();

    /**
     * Configurable success rate (90% success, 10% failure).
     */
    private static final double SUCCESS_RATE = 0.9;

    // ─── PROCESS PAYMENT ─────────────────────────────────────────

    /**
     * Processes a payment for an order.
     *
     * Step by step:
     * 1. Validate order exists and is PENDING
     * 2. Check no payment already exists for this order
     * 3. Parse the payment method
     * 4. Generate a unique transaction ID
     * 5. Simulate payment processing (90% success rate)
     * 6. If successful: mark payment COMPLETED, order CONFIRMED
     * 7. If failed: mark payment FAILED, order stays PENDING
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Long orderId = request.getOrderId();

        // 1. Validate order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order is not in PENDING status. Current status: " + order.getStatus());
        }

        // 2. Check for existing payment
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Payment already exists for order " + orderId);
        }

        // 3. Parse payment method
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid payment method: " + request.getPaymentMethod()
                            + ". Valid values: CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING");
        }

        // 4. Generate transaction ID
        String transactionId = generateTransactionId();

        // 5. Simulate payment processing
        boolean success = random.nextDouble() < SUCCESS_RATE;

        // 6. Create payment record
        Payment payment = Payment.builder()
                .transactionId(transactionId)
                .order(order)
                .amount(order.getTotalAmount())
                .method(method)
                .status(success ? PaymentStatus.COMPLETED : PaymentStatus.FAILED)
                .paidAt(success ? LocalDateTime.now() : null)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // 7. Update order status if payment successful
        if (success) {
            orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED);
            log.info("Payment COMPLETED for order {}: txn={}, amount={}",
                    order.getOrderNumber(), transactionId, order.getTotalAmount());
        } else {
            log.warn("Payment FAILED for order {}: txn={}",
                    order.getOrderNumber(), transactionId);
        }

        // 8. Publish events
        publishHistory(savedPayment, ActionType.CREATE);
        publishDomainEvent(savedPayment, success ? "PAYMENT_COMPLETED" : "PAYMENT_FAILED");

        return PaymentResponse.fromEntity(savedPayment);
    }

    // ─── GET PAYMENT STATUS ──────────────────────────────────────

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return PaymentResponse.fromEntity(payment);
    }

    // ─── Helpers ─────────────────────────────────────────────────

    /**
     * FACTORY PATTERN: Generates a unique transaction ID.
     * Format: TXN-YYYYMMDD-XXXXXXXX (8 random hex chars)
     */
    private String generateTransactionId() {
        String date = LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TXN-" + date + "-" + random;
    }

    private void publishHistory(Payment payment, ActionType actionType) {
        historyProducer.sendEvent(new HistoryEvent(
                Payment.class.getSimpleName(),
                payment.getId(),
                actionType.name()
        ));
    }

    private void publishDomainEvent(Payment payment, String eventType) {
        String json = String.format(
                "{\"type\":\"%s\",\"paymentId\":%d,\"transactionId\":\"%s\",\"orderId\":%d,\"amount\":%.2f,\"status\":\"%s\",\"method\":\"%s\",\"timestamp\":\"%s\"}",
                eventType,
                payment.getId(),
                payment.getTransactionId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getMethod(),
                LocalDateTime.now()
        );
        domainEventProducer.publish("payment-events", json);
    }
}
