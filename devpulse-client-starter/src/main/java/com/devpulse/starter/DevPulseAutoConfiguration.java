package com.devpulse.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
/**
 * Auto-configuration class for DevPulse client starter
 *
 * Spring Boot discovers this via the file:
 * META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 *
 *
 * CONDITIONAL ANNOTATIONS
 *
 * @ConditionalOnWebApplication
 * only activate in web apps (not cli tools and batch jobs)
 * the exception handler  only makes sense in a web context
 *
 * @ConditionalOnProperty
 * only activate if devpuulse.ingestion.enabled=true or if the property isn't set at all(matchIfMissing = true)
 * setting enabled=false completely disables DevPulse in test environments.
 *
 * @EnableConfigurationProperties(DevPulseProperties.class)
 * Registers the DevPulseProperties as Spring bean
 *
 * @ConditionalOnMissingBean
 * if consumer app defines their own DevPulseClient bean,
 * use their instead of ours. (convention over configuration)
 * Only create if user hasn’t created their own.
 * */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(
        prefix = "devpulse.ingestion",
        name = "enabled",
        matchIfMissing = true
)
@EnableConfigurationProperties(DevPulseProperties.class)
public class DevPulseAutoConfiguration {

    /**
     * The http client - core of the starter
     * Created first, injected into everything else
     * */
    @Bean
    @ConditionalOnMissingBean
    public DevPulseClient devPulseClient(DevPulseProperties properties){
        return new DevPulseClient(properties);
    }

    /**
     * Global exception handler
     * Only activated in web application
     * Only created if one doesn't already exist
     * */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    public DevPulseExceptionHandler devPulseExceptionHandler(
            DevPulseClient devPulseClient ){
        return new DevPulseExceptionHandler(devPulseClient);
    }

    /**
     * LogBack appender bean
     * Consuming app can wire this into their logback-spring.xml
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "ch.qos.logback.classic.Logger")
    public DevPulseLogbackAppender devPulseLogbackAppender(
            DevPulseClient devPulseClient){

         DevPulseLogbackAppender appender = new DevPulseLogbackAppender();
         appender.setDevPulseClient(devPulseClient);
         return appender;
     }
}
