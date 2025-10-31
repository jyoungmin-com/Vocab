package jyoungmin.vocabcommons.exception;

import jakarta.servlet.http.HttpServletRequest;
import jyoungmin.vocabcommons.constants.LoggingConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Base exception handler providing common exception handling across all services.
 * Extend this class in individual services and add service-specific exception handlers.
 * Handles validation errors, database exceptions, and generic errors with consistent formatting.
 */
@Slf4j
@RestControllerAdvice
public abstract class BaseGlobalExceptionHandler {

    /**
     * Handles custom business logic exceptions from services.
     * Logs at appropriate level based on HTTP status (4xx=warn, 5xx=error).
     * Override this method in subclasses if service-specific logging is needed.
     *
     * @param e       the service exception
     * @param request the HTTP request
     * @return error response with appropriate status
     */
    @ExceptionHandler(BaseServiceException.class)
    public ResponseEntity<ErrorResponse> handleBaseServiceException(
            BaseServiceException e,
            HttpServletRequest request) {

        String username = MDC.get(LoggingConstants.USERNAME_LOG_KEY);
        String userInfo = username != null ? " | User: " + username : "";

        // Log at appropriate level based on http status (4xx=warn, 5xx=error)
        if (e.getErrorCode().getHttpStatus().is5xxServerError()) {
            log.error("[{}]{} | URI: {} | Code: {}, Message: {}, Details: {}",
                    e.getClass().getSimpleName(), userInfo, request.getRequestURI(),
                    e.getErrorCode().getCode(),
                    e.getErrorCode().getMessage(),
                    e.getDetails());
        } else {
            log.warn("[{}]{} | URI: {} | Code: {}, Message: {}, Details: {}",
                    e.getClass().getSimpleName(), userInfo, request.getRequestURI(),
                    e.getErrorCode().getCode(),
                    e.getErrorCode().getMessage(),
                    e.getDetails());
        }

        ErrorResponse errorResponse = ErrorResponse.of(
                e.getErrorCode(),
                e.getDetails(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(errorResponse);
    }

    /**
     * Handles Bean Validation (@Valid) failures.
     * Sanitizes error messages to prevent information leakage (limits to first 3 errors, no field names in response).
     *
     * @param e       the validation exception
     * @param request the HTTP request
     * @return error response with validation details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        // Log detailed field errors for debugging (includes field names)
        String detailedErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("[MethodArgumentNotValidException] Validation failed: {}", detailedErrors);

        // Return sanitized error message (limit to first 3 errors, no field names to avoid information leakage)
        String errorDetails = e.getBindingResult().getFieldErrors().stream()
                .limit(3)
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        int errorCount = e.getBindingResult().getFieldErrorCount();
        if (errorCount > 3) {
            errorDetails += " (and " + (errorCount - 3) + " more validation error(s))";
        }

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT,
                errorDetails,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * Handles database constraint violations.
     * Detects specific constraint types (unique, not null) and maps to appropriate error codes.
     * Override this method in subclasses if service-specific constraint handling is needed.
     *
     * @param e       the data integrity violation exception
     * @param request the HTTP request
     * @return error response with appropriate error code
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException e,
            HttpServletRequest request) {

        String username = MDC.get(LoggingConstants.USERNAME_LOG_KEY);
        String userInfo = username != null ? " | User: " + username : "";

        ErrorCode errorCode = ErrorCode.DATABASE_ERROR;
        String errorMessage = "Database constraint violation";

        // Determine error type by examining the root cause
        Throwable rootCause = e.getRootCause();
        if (rootCause != null) {
            String rootMessage = rootCause.getMessage();
            if (rootMessage != null) {
                String lowerMessage = rootMessage.toLowerCase();

                // 1. Username unique constraint violation
                if ((lowerMessage.contains("duplicate") || lowerMessage.contains("unique"))
                        && lowerMessage.contains("username")) {
                    errorCode = ErrorCode.USERNAME_ALREADY_EXISTS;
                    errorMessage = "Please try a different username";
                }
                // 2. NOT NULL constraint violations
                else if (lowerMessage.contains("not null") || lowerMessage.contains("cannot be null")) {
                    errorCode = ErrorCode.MISSING_REQUIRED_FIELD;
                    errorMessage = "Required field is missing";
                }
                // 3. Other unique constraint violations
                else if (lowerMessage.contains("duplicate") || lowerMessage.contains("unique")) {
                    errorCode = ErrorCode.INVALID_INPUT;
                    errorMessage = "A record with this value already exists";
                }
                // 4. Other constraint violations - treat as server errors
                else {
                    errorCode = ErrorCode.DATABASE_ERROR;
                    errorMessage = "Database constraint violation";
                }
            }
        }

        // Log at appropriate level based on error code (4xx=warn, 5xx=error)
        if (errorCode.getHttpStatus().is5xxServerError()) {
            log.error("[DataIntegrityViolationException]{} | URI: {} | Error: {}",
                    userInfo, request.getRequestURI(), e.getMessage(), e);
        } else {
            log.warn("[DataIntegrityViolationException]{} | URI: {} | Error: {}",
                    userInfo, request.getRequestURI(), e.getMessage());
        }

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                errorMessage,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * Handles general database access errors.
     *
     * @param e       the data access exception
     * @param request the HTTP request
     * @return error response with database error code
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(
            DataAccessException e,
            HttpServletRequest request) {

        log.error("[DataAccessException] Database error: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.DATABASE_ERROR,
                "Database operation failed",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.DATABASE_ERROR.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * Handles illegal argument exceptions (legacy validation errors).
     * Override this method in subclasses if service-specific validation handling is needed.
     *
     * @param e       the illegal argument exception
     * @param request the HTTP request
     * @return error response with validation error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request) {

        String username = MDC.get(LoggingConstants.USERNAME_LOG_KEY);
        String userInfo = username != null ? " | User: " + username : "";

        log.warn("[IllegalArgumentException]{} | URI: {} | Validation failed: {}",
                userInfo, request.getRequestURI(), e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT,
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * Handles malformed JSON in request body.
     *
     * @param e       the message not readable exception
     * @param request the HTTP request
     * @return error response indicating malformed JSON
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e,
            HttpServletRequest request) {

        log.warn("[HttpMessageNotReadableException] Malformed JSON: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT,
                "Malformed JSON request. Please check your request format.",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * Handles unsupported HTTP method errors.
     *
     * @param e       the method not supported exception
     * @param request the HTTP request
     * @return error response indicating unsupported method
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request) {

        log.warn("[HttpRequestMethodNotSupportedException] Method not supported: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT,
                "HTTP method '" + e.getMethod() + "' is not supported for this endpoint.",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * Handles all uncaught exceptions (fallback handler).
     * Logs full stack trace and returns generic error message.
     *
     * @param e       the exception
     * @param request the HTTP request
     * @return error response with generic server error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception e,
            HttpServletRequest request) {

        log.error("[Exception] Unexpected error occurred: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(errorResponse);
    }
}
