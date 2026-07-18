package com.irp.sdk.spring.web;

import com.irp.sdk.core.IncidentClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Automatic latency capture for every HTTP request. Also the place a trace/request id
 * is established: it reuses an incoming "X-Request-Id" header if the caller already has
 * one (common behind a gateway/load balancer), otherwise generates a UUID, and puts it
 * in MDC under "traceId" so DefaultIncidentClient can attach it to any event raised
 * during this request - including a captureException() call made further down the stack.
 */
public class IncidentLatencyCaptureFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_MDC_KEY = "traceId";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final IncidentClient client;
    private final Duration slowThreshold;

    public IncidentLatencyCaptureFilter(IncidentClient client, Duration slowThreshold) {
        this.client = client;
        this.slowThreshold = slowThreshold;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String traceId = resolveTraceId(request);
        MDC.put(TRACE_ID_MDC_KEY, traceId);
        long start = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = Duration.ofNanos(System.nanoTime() - start).toMillis();
            if (durationMs >= slowThreshold.toMillis()) {
                client.trackEvent("http.request", Map.of(
                        "method", request.getMethod(),
                        "path", request.getRequestURI(),
                        "status", response.getStatus(),
                        "durationMs", durationMs));
            }
            MDC.remove(TRACE_ID_MDC_KEY); // don't leak into whatever request reuses this thread next
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String incoming = request.getHeader(REQUEST_ID_HEADER);
        return (incoming != null && !incoming.isBlank()) ? incoming : UUID.randomUUID().toString();
    }
}
