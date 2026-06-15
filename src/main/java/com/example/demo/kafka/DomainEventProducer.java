package com.example.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  DomainEventProducer                                          ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Publishes domain-specific events (order created, inventory   ║
 * ║  updated, shipment created, payment completed) to Kafka.      ║
 * ║  These are SEPARATE from audit/history events — they carry    ║
 * ║  richer business data for downstream consumers (analytics,    ║
 * ║  notifications, etc.).                                        ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - Uses ObjectProvider<KafkaTemplate> for graceful degradation║
 * ║    when Kafka is disabled (same pattern as HistoryProducer)   ║
 * ║  - Accepts a topic name and a JSON string payload             ║
 * ║  - Services serialize their event data to JSON before calling ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Observer / Mediator                          ║
 * ║  - Decouples event producers from consumers                   ║
 * ║  - Kafka acts as the message broker (mediator)                ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DomainEventProducer {

    private final ObjectProvider<KafkaTemplate<String, String>> domainKafkaTemplateProvider;

    /**
     * Publishes a domain event to the specified Kafka topic.
     *
     * @param topic       Kafka topic name (e.g., "order-events")
     * @param jsonPayload JSON string containing the event data
     */
    public void publish(String topic, String jsonPayload) {
        KafkaTemplate<String, String> kafkaTemplate = domainKafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate == null) {
            log.debug("Kafka is disabled or unavailable; skipping domain event on topic={}", topic);
            return;
        }

        kafkaTemplate.send(topic, jsonPayload);
        log.info("Published domain event to topic={}: {}", topic, jsonPayload);
    }
}
