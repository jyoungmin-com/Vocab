package jyoungmin.vocabcommons.constants;

/**
 * Constants used for logging and MDC (Mapped Diagnostic Context).
 */
public class LoggingConstants {

    /**
     * HTTP header name for correlation ID
     */
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    /**
     * MDC key for correlation ID
     */
    public static final String CORRELATION_ID_LOG_KEY = "correlationId";

    /**
     * MDC key for username
     */
    public static final String USERNAME_LOG_KEY = "username";

    private LoggingConstants() {
        // Utility class - prevent instantiation
    }
}
