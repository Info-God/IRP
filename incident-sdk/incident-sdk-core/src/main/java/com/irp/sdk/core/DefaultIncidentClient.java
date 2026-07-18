package com.irp.sdk.core;

import com.irp.sdk.core.event.DeploymentEvent;
import com.irp.sdk.core.event.ErrorEvent;
import com.irp.sdk.core.event.LogEvent;
import com.irp.sdk.core.transport.EventDispatcher;
import com.irp.sdk.core.transport.HttpSender;
import com.irp.sdk.core.transport.RetryPolicy;
import com.irp.sdk.core.util.SafeExecutor;
import org.slf4j.MDC;

import java.time.Duration;
import java.util.Map;

/**
 * Deliberately has NO Spring annotations or interfaces - it's plain Java 21 so it works
 * identically whether it was built by hand or by the Spring Boot starter's auto-configuration.
 * Lifecycle is exposed as plain start()/stop() methods rather than implementing Spring's
 * SmartLifecycle directly, for the same reason: that would leak a Spring dependency into
 * a module that's supposed to work in any Java app.
 */
public final class DefaultIncidentClient implements IncidentClient {

    private final IncidentClientConfig config;
    private final EventDispatcher dispatcher;

    public DefaultIncidentClient(IncidentClientConfig config) {
        this.config = config;
        RetryPolicy retryPolicy = new RetryPolicy(config.maxRetryAttempts(), config.initialBackoff(), config.maxBackoff());
        HttpSender sender = new HttpSender(config, retryPolicy);
        this.dispatcher = new EventDispatcher(config, sender);
        this.dispatcher.start();
    }

    /** Package-visible constructor used by tests to inject a fake dispatcher. */
    DefaultIncidentClient(IncidentClientConfig config, EventDispatcher dispatcher) {
        this.config = config;
        this.dispatcher = dispatcher;
    }

    @Override
    public void captureException(Throwable throwable) {
        captureException(throwable, Map.of());
    }

    @Override
    public void captureException(Throwable throwable, Map<String, Object> metadata) {
        SafeExecutor.run("captureException", () -> {
            if (throwable == null) {
                return;
            }
            dispatcher.enqueue(ErrorEvent.from(throwable, config.serviceName(), metadata));
        });
    }

    @Override
    public void trackEvent(String name, Map<String, Object> metadata) {
        SafeExecutor.run("trackEvent", () -> {
            if (name == null || name.isBlank()) {
                return;
            }
            dispatcher.enqueue(LogEvent.trackEvent(name, config.serviceName(), currentTraceId(), metadata));
        });
    }

    @Override
    public void markDeployment(String version, String commitHash) {
        SafeExecutor.run("markDeployment", () -> {
            if (version == null || version.isBlank()) {
                return;
            }
            dispatcher.enqueue(DeploymentEvent.of(version, commitHash, config.serviceName()));
        });
    }

    @Override
    public void sendHealthStatus() {
        SafeExecutor.run("sendHealthStatus", () ->
                dispatcher.enqueue(LogEvent.health(config.serviceName(), config.environment())));
    }

    @Override
    public void flush(Duration timeout) {
        SafeExecutor.run("flush", () -> dispatcher.flush(timeout));
    }

    @Override
    public long droppedEventCount() {
        return dispatcher.droppedCount();
    }

    /** Not part of the public IncidentClient contract - exposed for the starter's health indicator. */
    public java.time.Instant lastSuccessfulSend() {
        return dispatcher.lastSuccessfulSend();
    }

    public void start() {
        dispatcher.start();
    }

    public void stop(Duration flushTimeout) {
        dispatcher.stop(flushTimeout);
    }

    /** Reads whatever trace/request id the host application has already put in MDC
     *  (the Spring starter's latency filter does this - see IrpLatencyCaptureFilter),
     *  so events issued during a request are correlated with it. Never fails: no MDC
     *  key present just means no trace id is attached, which is a normal case. */
    private String currentTraceId() {
        try {
            return MDC.get("traceId");
        } catch (Exception e) {
            return null;
        }
    }
}
