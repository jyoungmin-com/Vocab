package jyoungmin.vocabcommons.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.MDC;

import java.time.LocalDateTime;

/**
 * Standardized error response structure for API error handling.
 * Includes correlation ID, timestamp, status codes, and error details.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Correlation ID for tracking requests across services
     */
    private final String correlationId;

    /**
     * Timestamp when the error occurred
     */
    private final String timestamp;

    /**
     * HTTP status code
     */
    private final int status;

    /**
     * HTTP status reason phrase
     */
    private final String error;

    /**
     * Application-specific error code
     */
    private final String code;

    /**
     * Human-readable error message
     */
    private final String message;

    /**
     * Additional error details, if available
     */
    private final String details;

    /**
     * Request path where the error occurred
     */
    private final String path;

    private static final String CORRELATION_ID_LOG_KEY = "correlationId";

    /**
     * Creates an error response from an error code.
     *
     * @param errorCode the error code
     * @return constructed error response
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .correlationId(MDC.get(CORRELATION_ID_LOG_KEY))
                .timestamp(LocalDateTime.now().toString())
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    /**
     * Creates an error response from an error code with additional details.
     *
     * @param errorCode the error code
     * @param details   additional error information
     * @return constructed error response
     */
    public static ErrorResponse of(ErrorCode errorCode, String details) {
        return ErrorResponse.builder()
                .correlationId(MDC.get(CORRELATION_ID_LOG_KEY))
                .timestamp(LocalDateTime.now().toString())
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .details(details)
                .build();
    }

    /**
     * Creates an error response from an error code with details and request path.
     *
     * @param errorCode the error code
     * @param details   additional error information
     * @param path      the request path where error occurred
     * @return constructed error response
     */
    public static ErrorResponse of(ErrorCode errorCode, String details, String path) {
        return ErrorResponse.builder()
                .correlationId(MDC.get(CORRELATION_ID_LOG_KEY))
                .timestamp(LocalDateTime.now().toString())
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .details(details)
                .path(path)
                .build();
    }
}
