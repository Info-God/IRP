package com.irp.sdk.spring.web;

import com.irp.sdk.core.IncidentClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Reports every uncaught controller exception automatically, then always returns null -
 * telling Spring "I did not handle this, keep looking." That's what makes this safe to
 * auto-register: it can never change the host application's actual error response or
 * interfere with its own @ExceptionHandlers. An SDK that alters error behavior is one
 * nobody would keep installed.
 */
public class IncidentExceptionCaptureResolver implements HandlerExceptionResolver {

    private final IncidentClient client;

    public IncidentExceptionCaptureResolver(IncidentClient client) {
        this.client = client;
    }

    @Override
    @Nullable
    public ModelAndView resolveException(@NonNull HttpServletRequest request,
                                          @NonNull HttpServletResponse response,
                                          @Nullable Object handler,
                                          @NonNull Exception ex) {
        client.captureException(ex, Map.of(
                "method", request.getMethod(),
                "path", request.getRequestURI()));
        return null;
    }
}
