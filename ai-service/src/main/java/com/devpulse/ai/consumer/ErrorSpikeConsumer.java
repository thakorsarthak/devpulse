package com.devpulse.ai.consumer;


import com.devpulse.ai.dto.ErrorSpikeMessage;
import com.devpulse.ai.service.IncidentAnalysisService;
import com.devpulse.ai.service.IncidentRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorSpikeConsumer {

    private final IncidentAnalysisService analysisService;

    @KafkaListener(
            topics = "error.spike.detected",
            groupId = "ai-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeErrorSpike(@Payload ErrorSpikeMessage message){
        log.info("Consumed error spike for service: {}" , message.getServiceName());

        try {
            analysisService.analyzeIncident(message);
        }
        catch (Exception e){
            log.error("failed to analyze incident: {}", e.getMessage(), e);
        }

    }
}
