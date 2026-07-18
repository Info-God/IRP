package com.irp.sdk.spring.autoconfigure;

import com.irp.sdk.core.IncidentClient;
import com.irp.sdk.spring.health.IncidentHealthIndicator;
import com.irp.sdk.spring.web.IncidentExceptionCaptureResolver;
import com.irp.sdk.spring.web.IncidentLatencyCaptureFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApplicationContextRunner spins up a minimal Spring context per test without a real
 * server - fast enough to run dozens of these, which is exactly what's needed to check
 * every @ConditionalOnProperty branch actually does what it claims.
 */
class IncidentAutoConfigurationTest {

    private static final String[] MINIMAL_CONFIG = {
            "incident.api-key=irp_live_test",
            "incident.endpoint=http://localhost:8080/api/v1/ingest",
            "incident.service-name=test-service"
    };

    private final ApplicationContextRunner nonWebRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(IncidentAutoConfiguration.class));

    private final WebApplicationContextRunner webRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(IncidentAutoConfiguration.class));

    @Test
    void doesNotRegisterAnythingWithoutAnApiKey() {
        nonWebRunner.run(context -> assertThat(context).doesNotHaveBean(IncidentClient.class));
    }

    @Test
    void doesNotRegisterAnythingWhenExplicitlyDisabled() {
        nonWebRunner
                .withPropertyValues(
                        "incident.enabled=false",
                        "incident.api-key=irp_live_test",
                        "incident.endpoint=http://localhost:8080/api/v1/ingest",
                        "incident.service-name=test-service")
                .run(context -> assertThat(context).doesNotHaveBean(IncidentClient.class));
    }

    @Test
    void registersIncidentClientWhenApiKeyIsConfigured() {
        nonWebRunner.withPropertyValues(MINIMAL_CONFIG).run(context -> {
            assertThat(context).hasSingleBean(IncidentClient.class);
            assertThat(context).hasSingleBean(IncidentClientLifecycle.class);
        });
    }

    @Test
    void doesNotRegisterWebCapturesInANonWebContext() {
        nonWebRunner.withPropertyValues(MINIMAL_CONFIG).run(context -> {
            assertThat(context).doesNotHaveBean(IncidentLatencyCaptureFilter.class);
            assertThat(context).doesNotHaveBean(IncidentExceptionCaptureResolver.class);
        });
    }

    @Test
    void registersWebCapturesInAServletWebContext() {
        webRunner.withPropertyValues(MINIMAL_CONFIG).run(context -> {
            assertThat(context).hasBean("incidentLatencyCaptureFilter");
            assertThat(context).hasSingleBean(IncidentExceptionCaptureResolver.class);
        });
    }

    @Test
    void latencyFilterCanBeDisabledIndependentlyOfErrorCapture() {
        webRunner.withPropertyValues(MINIMAL_CONFIG)
                .withPropertyValues("incident.latency-capture.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean("incidentLatencyCaptureFilter");
                    assertThat(context).hasSingleBean(IncidentExceptionCaptureResolver.class);
                });
    }

    @Test
    void registersActuatorHealthIndicatorWhenActuatorIsOnTheClasspath() {
        nonWebRunner.withPropertyValues(MINIMAL_CONFIG).run((AssertableApplicationContext context) ->
                assertThat(context).hasSingleBean(IncidentHealthIndicator.class));
    }

    @Test
    void doesNotRegisterHealthReporterByDefault() {
        nonWebRunner.withPropertyValues(MINIMAL_CONFIG).run(context ->
                assertThat(context).doesNotHaveBean(com.irp.sdk.spring.health.IncidentHealthReporter.class));
    }

    @Test
    void registersHealthReporterWhenAutoReportEnabled() {
        nonWebRunner.withPropertyValues(MINIMAL_CONFIG)
                .withPropertyValues("incident.health.auto-report.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(com.irp.sdk.spring.health.IncidentHealthReporter.class));
    }
}
