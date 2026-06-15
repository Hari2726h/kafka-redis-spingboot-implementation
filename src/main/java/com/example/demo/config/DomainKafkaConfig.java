package com.example.demo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  DomainKafkaConfig                                            ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  The existing KafkaConfig creates a KafkaTemplate<String,     ║
 * ║  HistoryEvent> which is specifically typed for audit events.   ║
 * ║  We need a SEPARATE KafkaTemplate<String, String> for domain  ║
 * ║  events (order-events, inventory-events, etc.) that carry     ║
 * ║  JSON string payloads.                                        ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - Creates its own ProducerFactory and KafkaTemplate beans    ║
 * ║    using String serializers (not JSON-typed like HistoryEvent)║
 * ║  - Creates a separate ConsumerFactory and listener factory    ║
 * ║    for domain events                                          ║
 * ║  - Registers the 4 new Kafka topics as Spring beans           ║
 * ║  - Only active when app.kafka.enabled=true                    ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Factory Pattern + Separation of Concerns     ║
 * ║  - Separates domain event pipeline from audit event pipeline  ║
 * ║  - Each pipeline has its own serialization strategy            ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Configuration
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true", matchIfMissing = false)
public class DomainKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    // ─── Producer ────────────────────────────────────────────────

    @Bean
    public ProducerFactory<String, String> domainProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> domainKafkaTemplate() {
        return new KafkaTemplate<>(domainProducerFactory());
    }

    // ─── Consumer ────────────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, String> domainConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "domain-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean(name = "domainKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> domainKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(domainConsumerFactory());
        return factory;
    }

    // ─── Topics ──────────────────────────────────────────────────

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name("order-events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic inventoryEventsTopic() {
        return TopicBuilder.name("inventory-events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic shipmentEventsTopic() {
        return TopicBuilder.name("shipment-events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name("payment-events").partitions(1).replicas(1).build();
    }
}
