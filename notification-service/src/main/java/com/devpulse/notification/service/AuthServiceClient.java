package com.devpulse.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import java.util.Map;


@Slf4j
@Component
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${notification.default.email:thakorsarthak2912@gmail.com}")
    private String defaultEmail;

    @Value("${internal.service.secret}")
    private String internalServiceSecret;

    public AuthServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public String getServiceOwnerEmail(String serviceName) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(authServiceUrl)
                    .path("/auth/service-owner")
                    .queryParam("serviceName", serviceName)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-internal-Secret" , internalServiceSecret);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, org.springframework.http.HttpMethod.GET, entity, Map.class);


            Map body = response.getBody();
            if (body != null && body.containsKey("email")) {
                String email = (String) body.get("email");
                log.info("Found owner email for service {}: {}", serviceName, email);
                return email;
            }

        } catch (Exception e) {
            log.warn("Could not fetch owner for service: {} — using default email. Error: {}",
                    serviceName, e.getMessage());
        }

        return defaultEmail;
    }
}