package com.devpulse.auth.repository;

import com.devpulse.auth.entity.ServiceRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRegistrationRepository
        extends JpaRepository<ServiceRegistration, Long> {

    @Query("SELECT s FROM ServiceRegistration s " +
            "JOIN FETCH s.user " +
            "WHERE s.serviceName = :serviceName")
    Optional<ServiceRegistration> findByServiceName(
            @Param("serviceName") String serviceName);
}