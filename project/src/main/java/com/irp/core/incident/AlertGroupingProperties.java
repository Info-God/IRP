package com.irp.core.incident;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "irp.alert-grouping")
public record AlertGroupingProperties(
        boolean enabled,
        long thresholdCount,
        long windowMinutes,
        long pollIntervalMs
) {
}
