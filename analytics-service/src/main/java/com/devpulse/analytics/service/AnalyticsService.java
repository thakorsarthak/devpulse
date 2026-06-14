package com.devpulse.analytics.service;

import com.devpulse.analytics.dto.PushEventMessage;
import com.devpulse.analytics.entity.PushActivity;
import com.devpulse.analytics.entity.RepositoryMetrics;
import com.devpulse.analytics.repository.PushActivityRepository;
import com.devpulse.analytics.repository.RepositoryMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Analytics service - processes push events and serves metrics.
 *
 * TWO RESPONSIBILITIES:
 * 1. Write path: process Kafka events, update DB
 * 2. Read path: serve aggregated metrics with Redis caching
 *
 * WHY @CacheEvict ON WRITE:
 * When new data arrives, old cached metrics are stale.
 * @CacheEvict removes the cached entry so next read
 * fetches fresh data from DB and re-caches it.
 *
 * WHY @Cacheable ON READ:
 * First call hits PostgreSQL, result stored in Redis.
 * Subsequent calls within TTL window → served from Redis.
 * PostgreSQL never sees repeated identical queries.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    private final RepositoryMetricsRepository metricsRepository;
    private final PushActivityRepository  pushActivityRepository;

    @Transactional
    @CacheEvict(value = "repository-metrics" , key = "#message.repositoryFullName")
    public void processPushEvent(PushEventMessage message){
        log.info("Processing push event for repo: {}" , message.getRepositoryFullName());

        PushActivity activity = PushActivity.builder()
                .repositoryFullName(message.getRepositoryFullName())
                .senderLogin(message.getSenderLogin())
                .branch(message.getBranch())
                .commitCount(message.getCommitCount() != null ? message.getCommitCount() : 0)
                .eventId(message.getEventId())
                .pushedAt(message.getPushedAt() != null ? message.getPushedAt() : LocalDateTime.now())
                .build();

        pushActivityRepository.save(activity);

        // Upsert aggregated metrics        !!! CASE IF REPOSITORY ALREADY PRESENT !!!
        RepositoryMetrics metrics = metricsRepository
                .findByRepositoryFullName(message.getRepositoryFullName())
                .orElse(RepositoryMetrics.builder()
                        .repositoryFullName(message.getRepositoryFullName())
                        .repository_name(message.getRepositoryName())
                        .totalCommits(0)
                        .totalPushes(0)
                        .activeContributors(0)
                        .build());


        // Increment Counters

        metrics.setTotalPushes(metrics.getTotalPushes() + 1);
        metrics.setTotalCommits(metrics.getTotalCommits()
                + (message.getCommitCount() != null ? message.getCommitCount() : 0));
        metrics.setLastPushAt(message.getPushedAt() != null ? message.getPushedAt() : LocalDateTime.now());
        metrics.setLastPushedBy(message.getSenderLogin());


        //update active contributors (last 30 days)
        long contributors = pushActivityRepository
                .countDistinctContributorsSince(
                        message.getRepositoryFullName(),
                        LocalDateTime.now().minusDays(30));
        metrics.setActiveContributors((int) contributors);

        metricsRepository.save(metrics);
        log.info("Updated metrics for repo: {} totalCommits: {}",
                message.getRepositoryFullName(),
                metrics.getTotalCommits());
    }
}
