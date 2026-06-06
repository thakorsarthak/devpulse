package com.devpulse.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * DevPulse Config Server
 *
 * Single source of truth for all service configurations.
 * Every service in DevPulse reads its config from here on startup.
 *
 * @EnableConfigServer - activates Spring Cloud Config Server behavior.
 * Without this annotation, this is just a regular Spring Boot app.
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args){
        SpringApplication.run(ConfigServerApplication.class,args);
    }

}
