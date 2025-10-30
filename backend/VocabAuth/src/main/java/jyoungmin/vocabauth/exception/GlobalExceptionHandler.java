package jyoungmin.vocabauth.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * AuthException - Custom business logic exceptions
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(
            AuthException e,
            HttpServletRequest request) {

        log.warn("[AuthException] Code: {}, Message: {}, Details: {}",
                e.getErrorCode().getCode(),
                e.getErrorCode().getMessage(),
                e.getDetails());

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
     * MethodArgumentNotValidException - @Valid validation failures
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        String errorDetails = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("[MethodArgumentNotValidException] Validation failed: {}", errorDetails);

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
     * BadCredentialsException - Invalid username/password
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException e,
            HttpServletRequest request) {

        log.warn("[BadCredentialsException] Invalid login attempt: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_CREDENTIALS,
                "Please check your username and password",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_CREDENTIALS.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * AuthenticationException - General authentication failures
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException e,
            HttpServletRequest request) {

        log.warn("[AuthenticationException] Authentication failed: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_TOKEN,
                "Authentication failed",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_TOKEN.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * DataIntegrityViolationException - Duplicate username, etc.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException e,
            HttpServletRequest request) {

        log.warn("[DataIntegrityViolationException] Data integrity violation: {}", e.getMessage());

        // Check if it's a duplicate key error
        ErrorCode errorCode = e.getMessage().contains("Duplicate")
                ? ErrorCode.USERNAME_ALREADY_EXISTS
                : ErrorCode.DATABASE_ERROR;

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                "Please try a different username",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * DataAccessException - Database errors
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
     * IllegalArgumentException - Legacy validation errors
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request) {

        log.warn("[IllegalArgumentException] Validation failed: {}", e.getMessage());

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
     * Exception - Generic exception handler (fallback)
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
