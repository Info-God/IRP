package com.irp.sdk.core;

import java.time.Duration;
import java.util.Objects;

/**
 * Plain configuration object - no Spring involved, so a non-Spring Java 21 app can
 * construct an IncidentClient directly via {@code IncidentClientConfig.builder()...}.
 * The Spring starter's IncidentProperties gets converted into one of these when it
 * builds the IncidentClient bean.
 */
public final class IncidentClientConfig {

    private final String apiKey;
    private final String endpoint;
    private final String serviceName;
    private final String environment;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final int queueCapacity;
    private final int batchSize;
    private final Duration flushInterval;
    private final int maxRetryAttempts;
    private final Duration initialBackoff;
    private final Duration maxBackoff;

    private IncidentClientConfig(Builder b) {
        this.apiKey = Objects.requireNonNull(b.apiKey, "apiKey is required");
        this.endpoint = normalizeEndpoint(Objects.requireNonNull(b.endpoint, "endpoint is required"));
        this.serviceName = Objects.requireNonNull(b.serviceName, "serviceName is required");
        this.environment = b.environment;
        this.connectTimeout = b.connectTimeout;
        this.readTimeout = b.readTimeout;
        this.queueCapacity = b.queueCapacity;
        this.batchSize = b.batchSize;
        this.flushInterval = b.flushInterval;
        this.maxRetryAttempts = b.maxRetryAttempts;
        this.initialBackoff = b.initialBackoff;
        this.maxBackoff = b.maxBackoff;
    }

    private static String normalizeEndpoint(String endpoint) {
        return endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
    }

    public String apiKey() { return apiKey; }
    public String endpoint() { return endpoint; }
    public String serviceName() { return serviceName; }
    public String environment() { return environment; }
    public Duration connectTimeout() { return connectTimeout; }
    public Duration readTimeout() { return readTimeout; }
    public int queueCapacity() { return queueCapacity; }
    public int batchSize() { return batchSize; }
    public Duration flushInterval() { return flushInterval; }
    public int maxRetryAttempts() { return maxRetryAttempts; }
    public Duration initialBackoff() { return initialBackoff; }
    public Duration maxBackoff() { return maxBackoff; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String apiKey;
        private String endpoint;
        private String serviceName;
        private String environment = "production";
        private Duration connectTimeout = Duration.ofSeconds(2);
        private Duration readTimeout = Duration.ofSeconds(3);
        private int queueCapacity = 2000;
        private int batchSize = 50;
        private Duration flushInterval = Duration.ofSeconds(2);
        private int maxRetryAttempts = 3;
        private Duration initialBackoff = Duration.ofMillis(200);
        private Duration maxBackoff = Duration.ofSeconds(5);

        public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }
        public Builder endpoint(String endpoint) { this.endpoint = endpoint; return this; }
        public Builder serviceName(String serviceName) { this.serviceName = serviceName; return this; }
        public Builder environment(String environment) { this.environment = environment; return this; }
        public Builder connectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; return this; }
        public Builder readTimeout(Duration readTimeout) { this.readTimeout = readTimeout; return this; }
        public Builder queueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; return this; }
        public Builder batchSize(int batchSize) { this.batchSize = batchSize; return this; }
        public Builder flushInterval(Duration flushInterval) { this.flushInterval = flushInterval; return this; }
        public Builder maxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; return this; }
        public Builder initialBackoff(Duration initialBackoff) { this.initialBackoff = initialBackoff; return this; }
        public Builder maxBackoff(Duration maxBackoff) { this.maxBackoff = maxBackoff; return this; }

        public IncidentClientConfig build() {
            return new IncidentClientConfig(this);
        }
    }
}
