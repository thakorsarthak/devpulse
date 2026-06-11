package com.devpulse.ingestion.service;


import com.devpulse.ingestion.dto.PushEventMessage;
import com.devpulse.ingestion.entity.WebhookEvent;
import com.devpulse.ingestion.kafka.IngestionKafkaProducer;
import com.devpulse.ingestion.repository.WebhookEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Core webhook processing logic.
 *
 * Processing order is critical:
 * 1. Check for duplicate delivery (idempotency)
 * 2. Save raw payload to DB (durability first)
 * 3. Parse payload
 * 4. Publish to Kafka
 * 5. Mark as published
 *
 * WHY SAVE BEFORE PUBLISHING TO KAFKA:
 * If Kafka is down, we still have the event in DB.
 * If we published first and DB save failed, we'd have an event in Kafka with no record in our system.
 * DB write is our source of truth.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {


    private final WebhookEventRepository webhookEventRepository;
    private final IngestionKafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;

    @Transactional
    public void processGitHubWebhook(String eventType , String deliveryId , String rawPayload){

        // Step 1: Idempotency check
        // CAUSE : GitHub retries webhooks that don't get 200 response
        // Same delivery_id = same event, skip it

        Optional<WebhookEvent> existing = webhookEventRepository.findByDeliveryId(deliveryId);

        if(existing.isPresent()){
            log.warn("Duplicate webhook delivery ignored: {}", deliveryId);
            return;
        }

        try {
            // Step 2: Parse payload to extract key fields
            JsonNode payload = objectMapper.readTree(rawPayload);

            String repoName = payload.path("repository")
                    .path("name").asText("unknown");

            String repoFullName =  payload.path("repository")
                    .path("full_name").asText("unknown");

            String senderLogin = payload.path("sender")
                    .path("login").asText("unknown");

            // Step 3 : saving raw event in db
            WebhookEvent event = WebhookEvent.builder()
                    .eventType(eventType)
                    .repositoryName(repoName)
                    .repositoryFullName(repoFullName)
                    .senderLogin(senderLogin)
                    .rawPayload(rawPayload)
                    .deliveryId(deliveryId)
                    .processed(false)
                    .kafkaPublished(false)
                    .build();

            WebhookEvent saved = webhookEventRepository.save(event);
            log.info("Saved Webhook event: {} for repo:{}", eventType , repoFullName);

        }
        catch (Exception e){
            log.error("Failed to process webhook: {}", e.getMessage(), e);
            // Don't rethrow - return 200 to GitHub
            // Failed events stay in DB with kafkaPublished=false
            // Retry job handles them
        }
    }


    private void processPushEvent(WebhookEvent saved , JsonNode payload){

        String branch = payload.path("refs").asText("")
                .replace("refs/heads" ,"");

        int commitCount = payload.path("commits").size();

        PushEventMessage message = PushEventMessage.builder()
                .eventId(saved.getId())
                .repositoryName(saved.getRepositoryName())
                .repositoryFullName(saved.getRepositoryFullName())
                .senderLogin(saved.getSenderLogin())
                .branch(branch)
                .commitCount(commitCount)
                .pushedAt(LocalDateTime.now())
                .build();

        //publish to kafka
        kafkaProducer.publishPushEvent(message);

        //marking as published
        saved.setKafkaPublished(true);
        saved.setProcessed(true);
        webhookEventRepository.save(saved);
    }
}
