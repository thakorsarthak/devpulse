package com.devpulse.ingestion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Stores raw GitHub webhook payloads.
 *
 * WHY STORE RAW PAYLOAD AS JSONB:
 * GitHub webhook payloads contain 100+ fields.
 * We only need a few now but requirements change.
 * JSONB lets us store everything and query later
 * without schema migrations every time we need a new field.
 *
 * WHY kafka_published FLAG:
 * If Kafka is temporarily down when we receive a webhook,
 * we save to DB first (guaranteed), then publish to Kafka.
 * A background job can retry unpublished events.
 * This is the "outbox pattern" — database as reliable buffer.
 */

@Entity
@Table(name = "webhook_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type" , nullable = false)
    private String eventType;

    @Column(name = "repository_name" , nullable = false)
    private String repositoryName;

    @Column(name = "repository_full_name" , nullable = false)
    private String repositoryFullName;

    @Column(name = "sender_login")
    private String senderLogin;

    // GitHub's unique ID for each webhook delivery
    // Used for idempotency - ignore duplicate deliveries
    @Column(name = "delivery_id", unique = true)
    private String deliveryId;

    // JSONB type - stored as JSON in postgreSQL
    // Allows querying - raw_payload --> 'ref' = 'refs/heads/main'
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload" , nullable = false , columnDefinition = "jsonb")
    private String rawPayload;

    @Column(nullable = false)
    @Builder.Default
    private Boolean processed = false;

    @Column(name = "kafka_published" , nullable = false)
    @Builder.Default
    private Boolean kafkaPublished = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false , updatable = false)
    private LocalDateTime createdAt;
}
