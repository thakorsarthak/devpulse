package com.devpulse.ingestion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Message published to Kafka topic: push.received
 *
 * WHY A SEPARATE DTO FOR KAFKA:
 * The entity has database-specific fields (id, kafkaPublished etc).
 * The Kafka message should only contain what consumers need.
 * Clean contract between producer and consumers.
 *
 * Analytics-service will deserialize this exact class.
 * Both services must have identical field names.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushEventMessage {

    private Long eventId;
    private String repositoryName;
    private String repositoryFullName;
    private String senderLogin;
    private String branch;
    private Integer commitCount;
    private LocalDateTime pushedAt;
}
