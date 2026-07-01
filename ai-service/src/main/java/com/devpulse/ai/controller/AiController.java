package com.devpulse.ai.controller;


/*
* we have manual testing point to which let us test RAG retrievel
* directly without going through the full kafka pipeline
* */

import com.devpulse.ai.service.CodeIngestionService;
import com.devpulse.ai.service.IncidentRetrievalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI Service" , description = "RAG-powered incident analysis")
public class AiController {

    private final IncidentRetrievalService retrievalService;

    private final CodeIngestionService codeIngestionService;

    @PostMapping("/search-similar")
    @Operation(summary = "test semantic search against chromaDB")
    public ResponseEntity<List<String>> searchSimilar(
            @RequestBody Map<String , String> request ){


        List<String> results = retrievalService.findSimilarIncident(request.get("errorMessage") ,
                request.getOrDefault("stackTrace","") , request.get("serviceName"));

        return  ResponseEntity.ok(results);
    }
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP", "service", "ai-service"));
    }

    @PostMapping("/ingest-codebase")
    @Operation(summary = "Embed a local codebase into ChromaDB for RAG context")
    public ResponseEntity<Map<String, Object>> ingestCodebase(
            @RequestBody Map<String, String> request) {

        String repoPath = request.get("repoPath");
        String repoName = request.getOrDefault("repoName", "unknown-repo");

        int count = codeIngestionService.ingestCodebase(repoPath, repoName);

        return ResponseEntity.ok(Map.of(
                "status", "completed",
                "filesIngested", count,
                "repoName", repoName
        ));
    }
}
