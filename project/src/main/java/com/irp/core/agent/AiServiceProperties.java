package com.irp.core.agent;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "irp.ai-service")
public record AiServiceProperties(
        String baseUrl,
        String internalToken,
        boolean autoTriggerEnabled
) {
}
