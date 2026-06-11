package com.devpulse.ingestion.repository;

import com.devpulse.ingestion.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    // Check for duplicate deliveries - GitHub retries webhooks on timeout , so same event can arrive twice
    Optional<WebhookEvent> findByDeliveryId(String deliveryId);

    // find events not yet published to Kafka - for retry job
    List<WebhookEvent> findByKafkaPublishedFalse();

    //recent event for specific repo
    List<WebhookEvent> findByRepositoryFullNameOrderByCreatedAtDesc(String repositoryFullName);
}
