package com.devpulse.analytics.repository;

import com.devpulse.analytics.entity.RepositoryMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryMetricsRepository extends JpaRepository<RepositoryMetrics, Long> {


    Optional<RepositoryMetrics> findByRepositoryFullName(String repositoryFullName);

    List<RepositoryMetrics> findAllByOrderByLastPushedAtDesc();

    List<RepositoryMetrics> findTop10ByOrderByTotalCommitsDesc();

}
