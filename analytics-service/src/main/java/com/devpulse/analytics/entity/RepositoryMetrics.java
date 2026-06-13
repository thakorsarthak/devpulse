package com.devpulse.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Aggregated metrics per repository.
 *
 * WHY UPSERT PATTERN:
 * When a push event arrives for a repo we've seen before,
 * we UPDATE the existing row (increment counters).
 * For a new repo, we INSERT.
 * This is the upsert pattern — findByRepoName, update
 * if present, create if not.
 *
 * THIS IS FOR DASHBOARD
 * */

@Getter
@Setter
@Entity
@Table(name = "repository_metrics")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryMetrics {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repository_full_name" , nullable = false , unique = true)
    private String repositoryFullName;

    @Column(name = "repository_name", nullable = false)
    private String repository_name;

    @Column(name = "total_commits" , nullable = false)
    @Builder.Default
    private Integer totalCommits = 0;

    @Column(name = "total_pushes" , nullable = false)
    @Builder.Default
    private Integer totalPushes = 0;

    @Column(name ="active_contributors" , nullable = false)
    @Builder.Default
    private Integer activeContributors = 0;

    @Column(name = "last_push_at")
    private LocalDateTime lastPushAt;

    @Column(name = "last_pushed_by")
    private String lastPushedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
