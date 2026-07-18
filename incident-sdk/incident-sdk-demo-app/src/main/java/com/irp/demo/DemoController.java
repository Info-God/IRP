package com.irp.demo;

import com.irp.sdk.core.IncidentClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Exercises all four public IncidentClient methods manually, plus two endpoints that
 * prove the STARTER's automatic behavior (latency capture, uncaught exception capture)
 * without calling the client directly at all.
 */
@RestController
public class DemoController {

    private final IncidentClient incidentClient;

    public DemoController(IncidentClient incidentClient) {
        this.incidentClient = incidentClient;
    }

    @GetMapping("/demo/track")
    public String track() {
        incidentClient.trackEvent("payment_failed", Map.of("orderId", "123"));
        return "tracked";
    }

    @GetMapping("/demo/error")
    public String error() {
        incidentClient.captureException(new RuntimeException("manually captured error"));
        return "captured";
    }

    @GetMapping("/demo/deploy")
    public String deploy() {
        incidentClient.markDeployment("v1.4.2", "abc123");
        return "deployment marked";
    }

    @GetMapping("/demo/health")
    public String health() {
        incidentClient.sendHealthStatus();
        return "health status sent";
    }

    /** No call to incidentClient here at all - IncidentExceptionCaptureResolver should
     *  still capture this automatically, and the response is still a normal 500 from
     *  Spring's default error handling, unaffected by the SDK. */
    @GetMapping("/demo/boom")
    public String boom() {
        throw new IllegalStateException("boom - uncaught, should be auto-captured");
    }

    /** Deliberately slow, to exercise IncidentLatencyCaptureFilter. */
    @GetMapping("/demo/slow")
    public String slow() throws InterruptedException {
        Thread.sleep(400);
        return "done";
    }

    @GetMapping("/demo/ping")
    public String ping() {
        return "pong";
    }
}
