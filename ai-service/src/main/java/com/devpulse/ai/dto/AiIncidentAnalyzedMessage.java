package com.devpulse.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Published to kafka topic: ai.incident.analysed
 * Consumed by notification-service to alert the  developer
 * */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiIncidentAnalyzedMessage {

    private Long errorLogId;
    private String serviceName;
    private String originalError;
    private String aiExplanation;
    private String suggestedFix;
    private boolean analysisSuccessful;
    private LocalDateTime analyzedAt;

}
