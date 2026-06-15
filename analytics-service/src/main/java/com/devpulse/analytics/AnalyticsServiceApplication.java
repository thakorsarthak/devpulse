package com.devpulse.analytics;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * DevPulse Analytics Service
 *
 * consumes kafka push events and builds aggregated metrics
 * redis caching on all read endpoints
 *
 * @EnableCaching activates Spring's caching abstraction
 * without this , @Cacheable annotations are ignored entirely*/
@SpringBootApplication
@EnableCaching
public class AnalyticsServiceApplication {
    public static  void main (String[] args){
        SpringApplication.run(AnalyticsServiceApplication.class , args);
    }

}
