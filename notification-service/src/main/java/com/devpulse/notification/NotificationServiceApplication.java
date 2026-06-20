package com.devpulse.notification;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DevPulse Notification Service
 * Consumes AI incident analysis results and sends email alerts.
 */
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args){
        SpringApplication.run(NotificationServiceApplication.class , args);
    }
}
