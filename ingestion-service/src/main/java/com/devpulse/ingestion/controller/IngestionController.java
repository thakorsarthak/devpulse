package com.devpulse.ingestion.controller;


import com.devpulse.ingestion.dto.ErrorLogRequest;
import com.devpulse.ingestion.entity.ErrorLog;
import com.devpulse.ingestion.service.ErrorLogService;
import com.devpulse.ingestion.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.TableGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Ingestion controller - two main endpoints:
 *
 * 1. POST /webhook/github
 *    Receives GitHub webhook events
 *    Called by GitHub servers directly
 *    Must return 200 quickly or GitHub retries
 *
 * 2. POST /errors
 *    Receives error logs from applications
 *    Called by your own services when errors occur
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Ingestion" , description = "Webhook and error log ingestion")
public class IngestionController {

    private final WebhookService webhookService;
    private  final ErrorLogService errorLogService;

    /**
     * GitHub webhook receiver.
     *
     * GitHub sends these headers with every webhook:
     * X-GitHub-Event: push / pull_request / deployment etc
     * X-GitHub-Delivery: unique UUID for this delivery
     * X-Hub-Signature-256: HMAC signature for verification
     *
     * WHY NOT VERIFY SIGNATURE IS REMAINING YET:
     * Signature verification requires a shared secret configured in GitHub webhook settings.
     * We add this later at the time of security hardening.
     * For now we accept all webhooks for testing.
     */

    @PostMapping("/webhook/github")
    @Operation(summary = "Receive GitHub webHook events")
    public ResponseEntity<Map<String,String>> receiveGitHubWebhook(
            @RequestHeader("X-GitHub-Event") String eventType,
            @RequestHeader(value = "X-GitHub-Delivery" , defaultValue = "unknow") String deliveryId ,
            @RequestBody String rawPayload){

        log.info("Received GitHub webhook: event={} delivery={}" , eventType , deliveryId);

        webhookService.processGitHubWebhook(
                eventType, deliveryId, rawPayload);

        return ResponseEntity.ok(
                Map.of("status", "received", "deliveryId", deliveryId));
    }


    /**
     * Error log ingestion endpoint.
     * Applications send their errors here for AI analysis.
     */
    @PostMapping("/errors")
    @Operation(summary = "Ingest error log for AI analysis")
    public ResponseEntity<Map<String,Object>> ingestError(
            @Valid @RequestBody ErrorLogRequest request){

        ErrorLog saved = errorLogService.ingestError(
                request.getServiceName(),
                request.getEnvironment(),
                request.getErrorMessage(),
                request.getStackTrace(),
                request.getSeverity()
        );

        return ResponseEntity.ok(Map.of(
                "status", "ingested",
                "errorLogId", saved.getId(),
                "aiAnalysisTriggered", !saved.getAiAnalyzed()
        ));
    }

    @GetMapping("/health")
    @Operation(summary = "Service health check")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP",
                "service", "ingestion-service"));
    }


}
