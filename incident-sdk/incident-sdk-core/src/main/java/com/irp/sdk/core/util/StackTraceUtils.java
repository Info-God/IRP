package com.irp.sdk.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class StackTraceUtils {

    /** irp-core stores this in a `text` column, but an SDK still shouldn't be able to
     *  ship an unbounded amount of data from one bad exception - cap it defensively. */
    private static final int MAX_LENGTH = 8_000;

    private StackTraceUtils() {
    }

    public static String render(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        String rendered = sw.toString();
        return rendered.length() > MAX_LENGTH
                ? rendered.substring(0, MAX_LENGTH) + "\n... [truncated]"
                : rendered;
    }
}
