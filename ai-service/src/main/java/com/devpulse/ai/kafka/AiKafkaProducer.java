package com.devpulse.ai.kafka;

import com.devpulse.ai.dto.AiIncidentAnalyzedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiKafkaProducer {

    private static final String TOPIC = "ai.incident.analyzed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAnalysis(AiIncidentAnalyzedMessage message){
        kafkaTemplate.send(TOPIC , message.getServiceName() , message)
                .whenComplete((result, ex) ->{
                    if(ex == null){
                        log.info("Published AI analysis for service: {}", message.getServiceName());
                    }else {
                        log.info("failed to publish AI analysis: {}", ex.getMessage() , ex);
                    }
                });
    }
}
