package com.devpulse.notification.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mirrors of ai-service's AiIncidentAnalyzedMessage exactly.
 * Field names must match for JSON deserialization to work.
 */
@Data
@Builder
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
