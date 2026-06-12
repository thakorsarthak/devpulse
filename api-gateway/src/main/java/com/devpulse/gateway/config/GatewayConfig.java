package com.devpulse.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;

import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.net.URI;
import java.util.List;

/**
 * Gateway routing configuration.
 *
 * Each route defines:
 * - WHAT path to match (predicate)
 * - WHERE to forward it (uri)
 * - WHAT to do before/after (filters)
 *
 * WHY CODE-BASED CONFIG OVER YAML:
 * Code gives us type safety and the ability to add
 * conditional logic.
 */
@Configuration
public class GatewayConfig {

    /**
     * CORS Configuration
     *
     * CORS = Cross-Origin Resource Sharing
     * Browsers block requests from different origins by default.
     * This config tells browsers: "DevPulse API allows these origins"
     */
    @Bean
    public CorsWebFilter corsWebFilter(){
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**",config);

        //CorsWebFilter is WebFlux filter which is belongs to SPRING WEBFLUX (Reactive)
        return new CorsWebFilter(source);

    }

}
