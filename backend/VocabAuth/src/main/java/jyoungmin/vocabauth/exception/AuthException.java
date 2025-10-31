package jyoungmin.vocabauth.exception;

import jyoungmin.vocabcommons.exception.ErrorCode;
import lombok.Getter;

/**
 * Custom exception for authentication and authorization errors.
 * Wraps error codes and provides detailed error information for auth-related failures.
 */
@Getter
public class AuthException extends RuntimeException {

    /**
     * The standardized error code associated with this exception
     */
    private final ErrorCode errorCode;

    /**
     * Additional error details for debugging and logging
     */
    private final String details;

    /**
     * Creates an auth exception with an error code.
     *
     * @param errorCode the error code
     */
    public AuthException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * Creates an auth exception with an error code and additional details.
     *
     * @param errorCode the error code
     * @param details   additional error information
     */
    public AuthException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage() + " - " + details);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * Creates an auth exception with an error code, details, and a root cause.
     *
     * @param errorCode the error code
     * @param details   additional error information
     * @param cause     the underlying exception that caused this error
     */
    public AuthException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage() + " - " + details, cause);
        this.errorCode = errorCode;
        this.details = details;
    }
}
