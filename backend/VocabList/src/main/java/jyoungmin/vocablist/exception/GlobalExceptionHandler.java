package jyoungmin.vocablist.exception;

import jakarta.servlet.http.HttpServletRequest;
import jyoungmin.vocabcommons.constants.LoggingConstants;
import jyoungmin.vocabcommons.exception.BaseGlobalExceptionHandler;
import jyoungmin.vocabcommons.exception.ErrorCode;
import jyoungmin.vocabcommons.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the vocabulary list service.
 * Extends base handler with vocabulary-specific exception handling.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    /**
     * Handles illegal argument exceptions with special handling for furigana validation.
     *
     * @param e       the illegal argument exception
     * @param request the HTTP request
     * @return error response with appropriate status
     */
    @Override
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request) {

        String username = MDC.get(LoggingConstants.USERNAME_LOG_KEY);
        String userInfo = username != null ? " | User: " + username : "";

        log.warn("[IllegalArgumentException]{} | URI: {} | Validation failed: {}",
                userInfo, request.getRequestURI(), e.getMessage());

        // Check if furigana validation (VocabList-specific)
        ErrorCode errorCode;
        if (e.getMessage() != null && e.getMessage().contains("requires furigana")) {
            errorCode = ErrorCode.FURIGANA_REQUIRED;
        } else {
            errorCode = ErrorCode.INVALID_INPUT;
        }

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }
}

