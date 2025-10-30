package jyoungmin.vocablist.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * VocabException - Custom business logic exceptions
     */
    @ExceptionHandler(VocabException.class)
    public ResponseEntity<ErrorResponse> handleVocabException(
            VocabException e,
            HttpServletRequest request) {

        log.warn("[VocabException] Code: {}, Message: {}, Details: {}",
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
     * FeignException.Unauthorized - Auth service returns 401
     */
    @ExceptionHandler(FeignException.Unauthorized.class)
    public ResponseEntity<ErrorResponse> handleFeignUnauthorizedException(
            FeignException.Unauthorized e,
            HttpServletRequest request) {

        log.warn("[FeignException.Unauthorized] Auth service authentication failed: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_TOKEN,
                "Authentication with auth service failed",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_TOKEN.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * FeignException - Other Feign client errors
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException e,
            HttpServletRequest request) {

        log.error("[FeignException] Auth service communication error: Status={}, Message={}",
                e.status(), e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.AUTH_SERVICE_ERROR,
                "Failed to communicate with authentication service",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.AUTH_SERVICE_ERROR.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * IllegalArgumentException - Legacy validation errors
     * Gradually migrate to VocabException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request) {

        log.warn("[IllegalArgumentException] Validation failed: {}", e.getMessage());

        // Check if it's furigana validation
        ErrorCode errorCode = e.getMessage().contains("furigana")
                ? ErrorCode.FURIGANA_REQUIRED
                : ErrorCode.INVALID_INPUT;

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
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

