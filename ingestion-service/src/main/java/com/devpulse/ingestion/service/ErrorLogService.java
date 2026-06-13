package com.devpulse.ingestion.service;


import com.devpulse.ingestion.dto.ErrorSpikeMessage;
import com.devpulse.ingestion.entity.ErrorLog;
import com.devpulse.ingestion.kafka.IngestionKafkaProducer;
import com.devpulse.ingestion.repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Processes error logs sent by applications.
 *
 * WHY ERROR THRESHOLD BEFORE PUBLISHING:
 * A single error might be a blip — network hiccup, transient failure.
 * We only trigger AI analysis when errors are persistent.
 * threshold = 1 for now (every error triggers AI analysis)
 * In production: set to 5+ errors in 10 minutes.
 *
 * This prevents alert fatigue — a real production concern.
 *
 * We implemented error thresholding to prevent alert storms.
 * Individual errors are logged but AI analysis only triggers when error count crosses a meaningful threshold."
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorLogService {

    private final ErrorLogRepository errorLogRepository;
    private final IngestionKafkaProducer kafkaProducer;

    // threshold before triggering AI analysis
    private static final long ERROR_THRESHOLD = 1;


    @Transactional
    public ErrorLog ingestError(String serviceName,
                                   String environment,
                                   String errorMessage,
                                   String stackTrace,
                                   String severity){

        // for log save
        ErrorLog errorLog = ErrorLog.builder()
                .serviceName(serviceName)
                .environment(environment)
                .errorMessage(errorMessage)
                .stackTrace(stackTrace)
                .severity(severity)
                .resolved(false)
                .aiAnalyzed(false)
                .build();

        ErrorLog saved = errorLogRepository.save(errorLog);
        log.info("Saved error log for service: {} severity: {}",
                serviceName, severity);

        // check error count for this service
        long unresolvedCount = errorLogRepository.countByServiceNameAndResolvedFalse(serviceName);


        // publish to kafka if threshold is crossed the limit
        if(unresolvedCount >= ERROR_THRESHOLD ){
            ErrorSpikeMessage message = ErrorSpikeMessage.builder()
                    .errorLogId(saved.getId())
                    .serviceName(serviceName)
                    .environment(environment)
                    .stackTrace(stackTrace)
                    .severity(severity)
                    .detectedAt(LocalDateTime.now())
                    .build();

            kafkaProducer.publishErrorSpike(message);
            log.info("Publish error spike for service: {} count: {}", serviceName,unresolvedCount);
        }

        return  saved;

    }
}
