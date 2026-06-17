package com.devpulse.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * It is same as ingestion-service's ErrorSpikeMessage exactly.
 * Field names must match for Kafka JSON deserialization to work.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorSpikeMessage {

    private Long errorLogId;
    private String serviceName;
    private String environment;
    private String errorMessage;
    private String stackTrace;
    private String severity;
    private LocalDateTime detectedAt;
}