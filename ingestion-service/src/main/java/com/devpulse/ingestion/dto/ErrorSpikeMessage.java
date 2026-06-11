package com.devpulse.ingestion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Message published to Kafka topic: error.spike.detected
 * Consumed by ai-service for RAG analysis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorSpikeMessage {

    private Long errorLogId;
    private String serviceName;
    private String environment;
    private String stackTrace;
    private String severity;
    private LocalDateTime detectedAt;
}
