package com.devpulse.ingestion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Stores error logs sent by applications for AI analysis.
 *
 * WHY A SEPARATE TABLE FROM WEBHOOK EVENTS:
 * Different data shape, different consumers.
 * Webhook events → analytics-service
 * Error logs → ai-service (RAG pipeline)
 * Keeping them separate means each consumer queries exactly what it needs.
 */
@Entity
@Table(name = "error_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name" , nullable = false)
    private String serviceName;

    @Column(nullable = false)
    @Builder.Default
    private String environment = "production";

    @Column(name = "error_message", nullable = false )
    private String errorMessage;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(nullable = false)
    @Builder.Default
    private String severity = "ERROR";

    @Column(nullable = false)
    @Builder.Default
    private Boolean resolved = false;

    @Column(name = "ai_analyzed", nullable = false)
    @Builder.Default
    private Boolean aiAnalyzed = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false,updatable = false)
    private LocalDateTime createdAt;

}
