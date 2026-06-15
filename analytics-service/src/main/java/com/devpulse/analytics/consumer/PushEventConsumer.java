package com.devpulse.analytics.consumer;


import com.devpulse.analytics.dto.PushEventMessage;
import com.devpulse.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushEventConsumer {

    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "push.received"
            , groupId = "analytics-group"
            , containerFactory = "kafkaListenerContainerFactory")
    public void consumePushEvent(
            @Payload PushEventMessage message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset){

        log.info("Consume push event: repo{} partition={} offset={}" ,
                message.getRepositoryFullName() , partition , offset);

        try {
            analyticsService.processPushEvent(message);
        } catch (Exception e) {
            // log and continue - not throwing
            // throwing causes Kafka to retry indefinitely
            log.error("Failed to process push event: {}",
                    e.getMessage(), e);
        }

    }
}
