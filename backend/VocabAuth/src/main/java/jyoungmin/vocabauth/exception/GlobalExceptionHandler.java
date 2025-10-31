package jyoungmin.vocabauth.exception;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import jyoungmin.vocabcommons.exception.BaseGlobalExceptionHandler;
import jyoungmin.vocabcommons.exception.ErrorCode;
import jyoungmin.vocabcommons.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the authentication service.
 * Extends base handler with authentication-specific exception handling.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    /**
     * Handles invalid login credentials.
     * Returns a generic error message to prevent username enumeration.
     *
     * @param e       the bad credentials exception
     * @param request the HTTP request
     * @return error response with appropriate status
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
     * Handles user not found during authentication.
     * Returns a generic error message to prevent username enumeration.
     *
     * @param e       the username not found exception
     * @param request the HTTP request
     * @return error response with appropriate status
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
            UsernameNotFoundException e,
            HttpServletRequest request) {

        log.warn("[UsernameNotFoundException] User not found: {}", e.getMessage());

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
     * Handles general authentication failures.
     *
     * @param e       the authentication exception
     * @param request the HTTP request
     * @return error response with appropriate status
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
     * Handles rate limit exceeded errors.
     * Returns 429 Too Many Requests status.
     *
     * @param e       the request not permitted exception
     * @param request the HTTP request
     * @return error response with 429 status
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRequestNotPermitted(
            RequestNotPermitted e,
            HttpServletRequest request) {

        log.warn("[RequestNotPermitted] Rate limit exceeded: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.RATE_LIMIT_EXCEEDED,
                "Too many requests. Please try again later.",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.RATE_LIMIT_EXCEEDED.getHttpStatus())
                .body(errorResponse);
    }

}
