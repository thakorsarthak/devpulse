package com.devpulse.gateway.filter;
/*This logs every request passing through the gateway — path, method, response time.*/

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Global filter - runs on EVERY request through the gateway.
 * Ordered - Lets you control filter execution order.
 *
 * WHY LOGGING AT GATEWAY LEVEL:
 * Centralized request logging means we see every API call in one place.
 * Individual services don't need their own request logging — gateway handles it.
 *
 * This is the "cross-cutting concern" pattern in action.
 *
 * Implements Ordered with highest precedence so this filter
 * runs first - before routing happens.
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange , GatewayFilterChain chain){

        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        // Log incoming requests
        log.info("{} {} [{}]", request.getMethod(),request.getPath(),request.getRemoteAddress());

        // chain.filter() = pass request to next filter/route
        // .then() = runs AFTER the downstream service responds
        return chain.filter(exchange).then
                (Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = exchange.getResponse().getStatusCode() != null ? exchange.getResponse().getStatusCode().value() : 0;
            log.info("<- {} {} [{}ms] status={}"
                    , request.getMethod()
                    , request.getPath()
                    , duration
                    , statusCode);
                    })
                );

    }
    @Override
    public int getOrder() {
        // Ordered.HIGHEST_PRECEDENCE = runs before all other filters
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
