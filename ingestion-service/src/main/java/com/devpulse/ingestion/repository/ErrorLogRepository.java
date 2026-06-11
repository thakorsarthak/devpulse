package com.devpulse.ingestion.repository;

import com.devpulse.ingestion.entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

    /**  serviceName is our partition key*/

    List<ErrorLog> findByAiAnalyzedFalse();

    List<ErrorLog> findByServiceNameOrderByCreatedAtDesc(String serviceName);

    long countByServiceNameAndResolvedFalse(String serviceName);

}
