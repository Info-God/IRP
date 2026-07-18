package com.irp.sdk.spring.autoconfigure;

import com.irp.sdk.core.DefaultIncidentClient;
import com.irp.sdk.core.IncidentClient;
import com.irp.sdk.core.IncidentClientConfig;
import com.irp.sdk.spring.health.IncidentHealthIndicator;
import com.irp.sdk.spring.health.IncidentHealthReporter;
import com.irp.sdk.spring.web.IncidentExceptionCaptureResolver;
import com.irp.sdk.spring.web.IncidentLatencyCaptureFilter;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * The single entry point registered in META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports.
 *
 * <p>Two separate conditions gate two separate things on purpose - {@code @ConditionalOnProperty}
 * cannot be repeated on one element, so "incident.enabled" gates the whole class, while
 * "incident.api-key must be present" gates only the {@link #incidentClient} bean itself.
 * Every other bean below then depends on {@code @ConditionalOnBean(IncidentClient.class)}
 * rather than re-checking the property directly, so a missing api-key quietly results in
 * NO beans at all instead of a startup failure from an unsatisfied @Autowired.
 *
 * <p>Also split into three independently-conditional groups: the client itself (works in
 * any app, web or not), web-only pieces (only in a servlet web app), and actuator pieces
 * (only if Actuator is on the classpath). Every bean is behind
 * {@code @ConditionalOnMissingBean} so a consumer can override any single piece.
 */
@AutoConfiguration
@EnableConfigurationProperties(IncidentProperties.class)
@ConditionalOnProperty(prefix = "incident", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IncidentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "incident", name = "api-key")
    public IncidentClient incidentClient(IncidentProperties properties) {
        return new DefaultIncidentClient(toClientConfig(properties));
    }

    @Bean
    @ConditionalOnBean(IncidentClient.class)
    IncidentClientLifecycle incidentClientLifecycle(IncidentClient incidentClient) {
        return new IncidentClientLifecycle(incidentClient);
    }

    @Bean
    @ConditionalOnBean(IncidentClient.class)
    @ConditionalOnProperty(prefix = "incident.health.auto-report", name = "enabled", havingValue = "true")
    public IncidentHealthReporter incidentHealthReporter(IncidentClient incidentClient, IncidentProperties properties) {
        return new IncidentHealthReporter(incidentClient, properties.health().autoReport().interval());
    }

    private static IncidentClientConfig toClientConfig(IncidentProperties p) {
        return IncidentClientConfig.builder()
                .apiKey(p.apiKey())
                .endpoint(p.endpoint())
                .serviceName(p.serviceName())
                .environment(p.environment())
                .queueCapacity(p.queue().capacity())
                .batchSize(p.queue().batchSize())
                .flushInterval(p.queue().flushInterval())
                .maxRetryAttempts(p.retry().maxAttempts())
                .initialBackoff(p.retry().initialBackoff())
                .maxBackoff(p.retry().maxBackoff())
                .build();
    }

    /**
     * Only activates inside a servlet web app - a CLI tool or batch job pulling in the
     * starter should not get a Filter or a HandlerExceptionResolver registered.
     *
     * <p>Gated on the "api-key" property directly rather than {@code @ConditionalOnBean(IncidentClient.class)}:
     * nested static member classes are parsed by Spring's ConfigurationClassParser
     * BEFORE the enclosing class's own @Bean methods are processed, so a
     * @ConditionalOnBean here would never see {@link #incidentClient} - it hasn't been
     * registered yet at the point this condition is evaluated. A property check has no
     * such ordering dependency.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "incident", name = "api-key")
    static class WebCaptureConfiguration {

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(prefix = "incident.latency-capture", name = "enabled", havingValue = "true", matchIfMissing = true)
        public FilterRegistrationBean<IncidentLatencyCaptureFilter> incidentLatencyCaptureFilter(
                IncidentClient incidentClient, IncidentProperties properties) {

            FilterRegistrationBean<IncidentLatencyCaptureFilter> registration = new FilterRegistrationBean<>(
                    new IncidentLatencyCaptureFilter(incidentClient, properties.latencyCapture().slowThreshold()));
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
            registration.setName("incidentLatencyCaptureFilter");
            return registration;
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(prefix = "incident.error-capture", name = "enabled", havingValue = "true", matchIfMissing = true)
        public IncidentExceptionCaptureResolver incidentExceptionCaptureResolver(IncidentClient incidentClient) {
            return new IncidentExceptionCaptureResolver(incidentClient);
        }
    }

    /** Only activates if Spring Boot Actuator is actually on the classpath. Property-gated
     *  for the same nested-class parsing-order reason as WebCaptureConfiguration above. */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(HealthIndicator.class)
    @ConditionalOnProperty(prefix = "incident", name = "api-key")
    static class ActuatorConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public IncidentHealthIndicator incidentHealthIndicator(IncidentClient incidentClient) {
            return new IncidentHealthIndicator(incidentClient);
        }
    }
}
