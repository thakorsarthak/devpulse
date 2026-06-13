package com.devpulse.ingestion.kafka;

import com.devpulse.ingestion.dto.ErrorSpikeMessage;
import com.devpulse.ingestion.dto.PushEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.KafkaClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for ingestion-service.
 *
 * WHY KafkaTemplate:
 * Spring's KafkaTemplate handles serialization, connection pooling, and retry logic.
 * We just call send() with topic name and payload.
 *
 * WHY CompletableFuture (async send):
 * Kafka send is non-blocking by default.
 * We don't wait for broker acknowledgment on the happy path.
 * The callback logs success or failure asynchronously.
 *
 * WHY LOG ON FAILURE BUT DON'T THROW:
 * The webhook event is already saved to PostgreSQL.
 * If Kafka publish fails, the outbox retry job will pick it up from the DB and republish.
 * Throwing here would return 500 to GitHub causing retries.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestionKafkaProducer {

    private static final String  PUSH_TOPIC = "push.recieved";
    private static final String ERROR_SPIKE_TOPIC = "error.spike.detected";

    private final KafkaTemplate<String, Object> kafkaTemplate;  //Spring's helper class for talking to Kafka.

    public void publishPushEvent(PushEventMessage message){

        /*Kafka sends messages asynchronously that's why CompletableFuture*/
        CompletableFuture<SendResult<String,Object>> future =
                kafkaTemplate.send(
                        PUSH_TOPIC,
                        message.getRepositoryFullName(), // our partition key
                        message
                );
        future.whenComplete((result,ex) ->{
            if(ex == null){
                log.info("Published push event for repo: {} offset: {}" ,
                        message.getRepositoryFullName() , result.getRecordMetadata().offset());
            }else {
                log.error("Failed to publish push event for repo: {}" ,
                        message.getRepositoryFullName(), ex);
            }
        });
    }


    public void publishErrorSpike(ErrorSpikeMessage message){

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        ERROR_SPIKE_TOPIC,
                        message.getServiceName(),   // our partition key
                        message
                );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published error spike for service: {}",
                        message.getServiceName());
            } else {
                log.error("Failed to publish error spike for service: {}",
                        message.getServiceName(), ex);
            }
        });

    }

}
