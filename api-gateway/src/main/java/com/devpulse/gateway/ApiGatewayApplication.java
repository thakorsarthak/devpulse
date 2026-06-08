package com.devpulse.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DevPulse API Gateway
 *
 * Single entry point for all client requests.
 * Built on Spring Cloud Gateway which uses
 * Spring WebFlux (reactive, non-blocking) under the hood.
 *
 * WHY REACTIVE HERE:
 * Gateway's job is routing — it spends most of its time
 * waiting for downstream services to respond.
 * Reactive (non-blocking) is perfect for this — one thread
 * can handle thousands of concurrent routing operations.
 * This is different from our other services which use
 * regular Spring MVC (blocking) because they do CPU work.
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args){
        SpringApplication.run(ApiGatewayApplication.class,args);
    }
}
