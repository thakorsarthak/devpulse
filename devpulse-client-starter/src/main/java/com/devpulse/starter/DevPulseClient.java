package com.devpulse.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP client that ships error report to devpulse ingestion-service
 *
 * SHORT TIMEOUTS:
 * If DevPulse ingestion-service is down, we must not block the consuming app's request thread waiting for a response.
 * 2s connect + 3s read = max 5s penalty, then we give up.
 * Reporting to DevPulse is best-effort, never blocking.
 *
 * WHY FIRE-AND-FORGET (no exception rethrown):
 * If DevPulse itself is down, the consuming app must continue
 * working normally. Errors in the error reporter must never
 * cause additional errors in the app being monitored.
 * */
public class DevPulseClient {
    private static final Logger log =
            LoggerFactory.getLogger(DevPulseClient.class);

    private final DevPulseProperties properties;
    private final RestTemplate restTemplate;

    public DevPulseClient(DevPulseProperties properties){
        this.properties = properties;
        this.restTemplate = buildRestTemplate();
    }

    public void reportError(String errorMessage ,
                            String stackTrace,
                            String severity){
        if(!properties.isEnabled()){
            return;
        }

        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("serviceName" , properties.getServiceName());
            payload.put("environment", properties.getEnvironment());
            payload.put("errorMessage", truncate(errorMessage, 1000));
            payload.put("stackTrace", truncate(stackTrace, 5000));
            payload.put("severity" , severity);

            restTemplate.postForEntity(
                    properties.getUrl() , payload, Map.class);

            log.debug("DevPulse: reported error for service: {}" , properties.getServiceName());

        }catch (Exception e){
            log.warn("DevPulse: failed to report error (ingestion-service "
                    +"may be down): {}", e.getMessage());
        }
    }

    public static String extractStackTrace(Throwable throwable){
        if(throwable == null) return "";
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private String truncate(String value , int maxLength){
        if(value == null) return "";
        return value.length() > maxLength
                ? value.substring(0 , maxLength) + "...[truncated]"
                : value;
    }

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMs()); //wait for 2 seconds to  connect
        factory.setReadTimeout(properties.getReadTimeoutMs()); // wait for 3 seconds for response
        RestTemplate template = new RestTemplate(factory);
        return template;
    }
}
