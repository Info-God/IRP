package com.irp.sdk.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The single mechanism behind "an SDK exception must never reach the user's application" -
 * every public IncidentClient method body runs through {@link #run}, so this guarantee
 * holds even for bugs in the SDK itself (a bad event, a null config field, anything),
 * not just for the expected failure modes that were specifically coded around.
 */
public final class SafeExecutor {

    private static final Logger log = LoggerFactory.getLogger(SafeExecutor.class);

    private SafeExecutor() {
    }

    public static void run(String actionName, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.warn("Incident SDK: '{}' failed internally and was suppressed so it can't affect " +
                    "your application. This is a bug in the SDK, not your code - please report it.",
                    actionName, e);
        }
    }
}
