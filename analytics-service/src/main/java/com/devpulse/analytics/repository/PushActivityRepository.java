package com.devpulse.analytics.repository;

import com.devpulse.analytics.entity.PushActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PushActivityRepository extends JpaRepository<PushActivity, Long> {
    List<PushActivity> findByRepositoryFullNameOrderByPushedAtDesc(String repositoryFullName);

    // count pushes in a time window - used for velocity metrics
    @Query("SELECT COUNT(p) FROM PushActivity p " +
            "WHERE p.repositoryFullName = :repo " +
            "AND p.pushedAt >= :since" )
    long countPushedSince(@Param("repo") String repositoryFullName ,
                          @Param("since") LocalDateTime since);

    // Distinct contributors in last 30 days
    @Query("SELECT COUNT(DISTINCT p.senderLogin) FROM PushActivity p " +
            "WHERE p.repositoryFullName = :repo " +
            "AND p.pushedAt >= :since")
    long countDistinctContributorsSince(
            @Param("repo") String repositoryFullName,
            @Param("since") LocalDateTime since);
}
