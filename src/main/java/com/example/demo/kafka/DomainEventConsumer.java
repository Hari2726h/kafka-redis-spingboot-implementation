package com.example.demo.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  DomainEventConsumer                                          ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Listens to all domain event topics and logs them. In a       ║
 * ║  real production system, each topic would have its own        ║
 * ║  dedicated consumer (e.g., NotificationService, Analytics).   ║
 * ║  Here we use a single consumer for demonstration/logging.     ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - Uses @KafkaListener with multiple topics                   ║
 * ║  - containerFactory points to "domainKafkaListenerContainer   ║
 * ║    Factory" which uses String deserializers                   ║
 * ║  - Only active when app.kafka.enabled=true                    ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Observer Pattern                             ║
 * ║  - This class is an "observer" that reacts to events          ║
 * ║  - Kafka decouples it from the services that produce events   ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Service
@Slf4j
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class DomainEventConsumer {

    @KafkaListener(
            topics = {"order-events", "inventory-events", "shipment-events", "payment-events"},
            groupId = "domain-group",
            containerFactory = "domainKafkaListenerContainerFactory"
    )
    public void consume(String message) {
        log.info("Received domain event: {}", message);

        // ─────────────────────────────────────────────────────────
        // In a production system, you would:
        // 1. Parse the JSON message
        // 2. Route to the appropriate handler based on event type
        // 3. Trigger downstream actions (send email, update dashboard, etc.)
        //
        // Example:
        //   JsonNode node = objectMapper.readTree(message);
        //   String type = node.get("type").asText();
        //   switch(type) {
        //       case "ORDER_CREATED" -> notificationService.notifyOrderCreated(node);
        //       case "PAYMENT_COMPLETED" -> analyticsService.recordPayment(node);
        //   }
        // ─────────────────────────────────────────────────────────
    }
}
