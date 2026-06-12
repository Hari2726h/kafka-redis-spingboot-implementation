package com.example.demo.kafka;

import com.example.demo.entity.History;
import com.example.demo.repository.HistoryRepository;    
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;  
import org.springframework.stereotype.Service;
import java.time.LocalDateTime; 

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class HistoryConsumer {

    private final HistoryRepository historyRepository;

    @KafkaListener(
            topics = "history-events",
            groupId = "history-group",
            containerFactory = "historyKafkaListenerContainerFactory"
    )
    public void consume(
            HistoryEvent event
    ) {

        log.info("Received {}",event);

        History history = new History();

        history.setObjectType(
                event.getObjectType()
        );

        history.setObjectId(
                event.getObjectId()
        );

        history.setAction(
                event.getAction()
        );

        history.setCreatedAt(
                LocalDateTime.now()
        );

        historyRepository.save(history);
    }
}