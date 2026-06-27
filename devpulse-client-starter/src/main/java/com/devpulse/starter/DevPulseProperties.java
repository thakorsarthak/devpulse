package com.devpulse.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for DevPulse Client starter.
 *
 * Spring Look for all properties starting with devpulse.ingestion and map them into this class.
 *
 * Spring automatically converts kebab-case → camelCase.
 *
 * Why @ConfigurationProperties OVER @Value
 * @ConfigurationProperties groups related properties together as a typed POJO.
 * IDE autocomplete works, validation works, and it's easier to pass around as a dependency.
 * @Value is fine for on-ff properties, not for group
 * */
@ConfigurationProperties(prefix = "devpulse.ingestion")
public class DevPulseProperties {

    /**
     * Full URL to DevPulse ingestion-service errors endpoint.
     */
    private String url = "http://localhost:8080/api/ingest/errors";

    /**
     * Name of the service using this starter.
     * Appears in DevPulse alerts so you know which app errored.
     * Defaults to spring.application.name if not set.
     */
    private String serviceName = "unknown-service";

    /**
     * Whether DevPulse reporting is enabled.
     * Set to false in test environments.
     * Its a Simple switch.
     */
    private boolean enabled = true;

    /**
     * Environment label shown in alerts.
     */
    private String environment = "production";

    /**
     * Connection timeout for HTTP calls to ingestion-service (ms).
     * Short timeout — DevPulse reporting should never block the main application's request thread.
     *
     * DevPulse server is down Without timeout Thread may hang forever.
     *
     * With timeout thread Wait 2 seconds max.
     */
    private int connectTimeoutMs = 2000;

    /**
     * Read timeout for HTTP calls to ingestion-service (ms).
     *
     * thread waits for 3 seconds for result
     */
    private int readTimeoutMs = 3000;

    // Getters and setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) {
        this.environment = environment; }

    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs; }

    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs; }

}
