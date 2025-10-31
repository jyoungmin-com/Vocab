package jyoungmin.vocabcommons.exception;

import lombok.Getter;

/**
 * Base exception class for service-layer business logic errors.
 * Provides structured error information with error codes and optional details.
 */
@Getter
public class BaseServiceException extends RuntimeException {

    /**
     * Error code identifying the type of error
     */
    private final ErrorCode errorCode;

    /**
     * Additional details about the error, if available
     */
    private final String details;

    /**
     * Creates an exception with an error code.
     *
     * @param errorCode the error code identifying the error type
     */
    public BaseServiceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * Creates an exception with an error code and additional details.
     *
     * @param errorCode the error code identifying the error type
     * @param details   additional information about the error
     */
    public BaseServiceException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage() + " - " + details);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * Creates an exception with an error code, details, and a cause.
     *
     * @param errorCode the error code identifying the error type
     * @param details   additional information about the error
     * @param cause     the underlying cause of this exception
     */
    public BaseServiceException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage() + " - " + details, cause);
        this.errorCode = errorCode;
        this.details = details;
    }
}
