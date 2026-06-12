package com.example.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryProducer {

        private final ObjectProvider<KafkaTemplate<String, HistoryEvent>> kafkaTemplateProvider;

        public void sendEvent(HistoryEvent event) {
                KafkaTemplate<String, HistoryEvent> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
                if (kafkaTemplate == null) {
                        log.debug("Kafka is disabled or unavailable; skipping history event {}", event);
                        return;
                }

                kafkaTemplate.send("history-events", event);
        }
}