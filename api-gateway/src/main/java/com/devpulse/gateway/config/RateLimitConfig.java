package com.devpulse.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Rate limiting configuration.
 *
 * Spring Cloud Gateway has built-in rate limiting via
 * Redis token bucket algorithm.
 *
 * WHY TOKEN BUCKET:
 * Each client gets a "bucket" of tokens. Each request
 * costs one token. Tokens refill at a fixed rate.
 * This allows short bursts while preventing sustained abuse.
 *
 * We identify clients by their IP address.
 * (In production: identify by JWT user ID for per-user limits.)
 *
 * NOTE: Full rate limiting requires Redis running.
 * We define the resolver now, activate it per-route when we add Redis to Docker Compose.
 */
@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver ipKeyResolver(){

        return exchange -> Mono.just(
                exchange.getRequest()
                        .getRemoteAddress()
                        .getAddress()
                        .getHostAddress()
        );
    }
}
