package jyoungmin.vocabcommons.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Standardized API response wrapper for successful operations.
 * Includes correlation ID, timestamp, status code, message, and optional data.
 * Excludes null fields from JSON serialization.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Correlation ID for tracking requests across services
     */
    private final String correlationId;

    /**
     * Timestamp when the response was created
     */
    private final String timestamp;

    /**
     * HTTP status code
     */
    private final int status;

    /**
     * Success message
     */
    private final String message;

    /**
     * Optional response data
     */
    private final T data;

    /**
     * MDC key for correlation ID
     */
    private static final String CORRELATION_ID_LOG_KEY = "correlationId";

    /**
     * Standard success message constants for consistency across controllers.
     * Provides predefined messages for common operations.
     */
    public static class Messages {
        // Auth
        public static final String LOGIN_SUCCESS = "Login successful";
        public static final String LOGOUT_SUCCESS = "Logout successful";
        public static final String REGISTER_SUCCESS = "User registered successfully";
        public static final String TOKEN_REFRESHED = "Token refreshed successfully";

        // Word
        public static final String WORD_CREATED = "Word created successfully";
        public static final String WORD_UPDATED = "Word updated successfully";
        public static final String WORD_DELETED = "Word deleted successfully";
        public static final String WORDS_RETRIEVED = "Words retrieved successfully";
        public static final String WORD_RETRIEVED = "Word retrieved successfully";

        // List
        public static final String LIST_CREATED = "List created successfully";
        public static final String LISTS_RETRIEVED = "Lists retrieved successfully";

        // User
        public static final String USER_INFO_RETRIEVED = "User information retrieved successfully";
        public static final String USERNAME_AVAILABLE = "Username is available";
        public static final String USERNAME_TAKEN = "Username is already taken";

        private Messages() {
            // Prevent instantiation
        }
    }

    /**
     * Creates a success response with custom HTTP status, message, and data.
     *
     * @param httpStatus the HTTP status
     * @param message    the success message
     * @param data       the response data
     * @param <T>        the type of response data
     * @return API response with all fields populated
     */
    public static <T> ApiResponse<T> success(HttpStatus httpStatus, String message, T data) {
        return ApiResponse.<T>builder()
                .correlationId(MDC.get(CORRELATION_ID_LOG_KEY))
                .timestamp(LocalDateTime.now().toString())
                .status(httpStatus.value())
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates a success response with custom HTTP status and message (no data).
     *
     * @param httpStatus the HTTP status
     * @param message    the success message
     * @param <T>        the type of response data
     * @return API response without data
     */
    public static <T> ApiResponse<T> success(HttpStatus httpStatus, String message) {
        return ApiResponse.<T>builder()
                .correlationId(MDC.get(CORRELATION_ID_LOG_KEY))
                .timestamp(LocalDateTime.now().toString())
                .status(httpStatus.value())
                .message(message)
                .build();
    }

    /**
     * Creates a success response with 200 OK status and message (no data).
     *
     * @param message the success message
     * @param <T>     the type of response data
     * @return API response with OK status
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .correlationId(MDC.get(CORRELATION_ID_LOG_KEY))
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message(message)
                .build();
    }

    /**
     * Creates a success response with 200 OK status, message, and data.
     *
     * @param message the success message
     * @param data    the response data
     * @param <T>     the type of response data
     * @return API response with OK status and data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .correlationId(MDC.get(CORRELATION_ID_LOG_KEY))
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .build();
    }
}
