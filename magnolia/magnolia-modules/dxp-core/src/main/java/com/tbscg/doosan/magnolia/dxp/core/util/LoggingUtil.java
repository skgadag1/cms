package com.tbscg.doosan.magnolia.dxp.core.util;

import org.apache.commons.lang3.function.FailableCallable;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import java.util.regex.Pattern;

/**
 * Utility class for logging helpers.
 */
public final class LoggingUtil {
    /**
     * Calls callable and logs its execution time. Then returns its result.
     *
     * @param failableCallable callable to call
     * @param logger           logger to use logging
     * @param taskName         task name to use in log message
     * @param <T>              result type
     * @return call result
     * @throws Exception forwarded from callable
     */
    public static <T> T callAndLogTiming(final FailableCallable<T, Exception> failableCallable, final Logger logger, final String taskName) throws Exception {
        final StopWatch stopWatch = StopWatch.createStarted();
        final T result = failableCallable.call();

        stopWatch.stop();
        logger.debug(taskName + " took: {}", stopWatch.formatTime());

        return result;
    }

    /**
     * Sanitizes message by replacing new line characters with their escaped versions.
     *
     * @param message message to sanitize
     * @return sanitized message
     */
    public static String sanitizeMessage(String message) {
        return message != null ? message.replace("\n", "\\n" ).replace("\r", "\\r" ) : null;
    }

}
