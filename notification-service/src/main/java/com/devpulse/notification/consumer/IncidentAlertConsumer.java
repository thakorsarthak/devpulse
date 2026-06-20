package com.devpulse.notification.consumer;

import com.devpulse.notification.dto.AiIncidentAnalyzedMessage;
import com.devpulse.notification.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 *
 * Consumes AI analysis results and triggers email alerts.
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentAlertConsumer {

    private final EmailNotificationService emailService;

    @KafkaListener(
            topics = "ai.incident.analyzed",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumerIncidentAnalysis(
            @Payload AiIncidentAnalyzedMessage  message){

        log.info("Received AI analysis for Service: {} success; {}" , message.getServiceName() , message.isAnalysisSuccessful());

        emailService.sendIncidentAlert(message);
    }
}
