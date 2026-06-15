package com.devpulse.analytics.controller;

import com.devpulse.analytics.entity.PushActivity;
import com.devpulse.analytics.entity.RepositoryMetrics;
import com.devpulse.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.shaded.io.opentelemetry.proto.metrics.v1.Summary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Analytics REST API.
 * Serves pre-aggregated metrics with Redis caching.
 * These endpoints are what a dashboard would call.
 */
@RestController
@RequestMapping("analytics")
@RequiredArgsConstructor
@Tag( name = "Analytics" , description = "Repository metrics and activity data")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/repositories")
    @Operation(summary = "Get all repository metrics")
    public ResponseEntity<List<RepositoryMetrics>> getAllRepositories(){
        return ResponseEntity.ok(analyticsService.getAllRepositoryMetrics());
    }

    @GetMapping("repositories/top")
    @Operation(summary = "Get top 10 repositories by commit count")
    public ResponseEntity<List<RepositoryMetrics>> getTopRepository(){
        return ResponseEntity.ok(analyticsService.getTopRepositories());
    }

    @GetMapping("/repositories/{owner}/{repo}")
    @Operation(summary = "Get matrics for a specific repositories")
    public ResponseEntity<RepositoryMetrics> getRepositoryMetrics(
            @PathVariable String owner,
            @PathVariable String repo){
        String fullName = owner + "/" + repo;
        return ResponseEntity.ok(analyticsService.getRepositoryMetrics(fullName));
    }

    @GetMapping("repositories/{owner}/{repo}/activity")
    @Operation(summary = "Get recent push activity for a repository")
    public ResponseEntity<List<PushActivity>> getRecentActivity(
            @PathVariable String owner,
            @PathVariable String repo){
        String fullName = owner + "/" + repo;
        return ResponseEntity.ok(analyticsService.getRecentActivity(fullName));

    }
}
