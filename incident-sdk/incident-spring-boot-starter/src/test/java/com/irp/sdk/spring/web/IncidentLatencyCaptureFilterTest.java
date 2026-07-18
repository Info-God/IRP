package com.irp.sdk.spring.web;

import com.irp.sdk.core.IncidentClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IncidentLatencyCaptureFilterTest {

    @Mock
    private IncidentClient client;

    @Test
    void reportsRequestMethodPathAndStatusWhenThresholdIsZero() throws Exception {
        IncidentLatencyCaptureFilter filter = new IncidentLatencyCaptureFilter(client, Duration.ZERO);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/checkout");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        filter.doFilter(request, response, (req, res) -> { /* simulate the rest of the chain */ });

        ArgumentCaptor<Map<String, Object>> metadataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).trackEvent(eq("http.request"), metadataCaptor.capture());
        assertThat(metadataCaptor.getValue()).containsEntry("method", "GET").containsEntry("path", "/checkout");
    }

    @Test
    void doesNotReportRequestsFasterThanTheSlowThreshold() throws Exception {
        IncidentLatencyCaptureFilter filter = new IncidentLatencyCaptureFilter(client, Duration.ofSeconds(30));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/fast");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        verify(client, never()).trackEvent(eq("http.request"), anyMap());
    }

    @Test
    void reusesAnIncomingRequestIdHeaderInsteadOfGeneratingANewOne() throws Exception {
        IncidentLatencyCaptureFilter filter = new IncidentLatencyCaptureFilter(client, Duration.ZERO);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/checkout");
        request.addHeader("X-Request-Id", "existing-trace-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        final String[] observedTraceId = new String[1];
        filter.doFilter(request, response, (req, res) -> observedTraceId[0] = org.slf4j.MDC.get("traceId"));

        assertThat(observedTraceId[0]).isEqualTo("existing-trace-id");
    }
}
