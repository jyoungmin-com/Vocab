package jyoungmin.vocablist.exception;

import jyoungmin.vocabcommons.exception.ErrorCode;
import lombok.Getter;

/**
 * Custom exception for vocabulary list service errors.
 * Wraps error codes and provides detailed error information for vocab-related failures.
 */
@Getter
public class VocabException extends RuntimeException {

    /**
     * The standardized error code associated with this exception
     */
    private final ErrorCode errorCode;

    /**
     * Additional error details for debugging and logging
     */
    private final String details;

    /**
     * Creates a vocabulary exception with an error code.
     *
     * @param errorCode the error code
     */
    public VocabException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * Creates a vocabulary exception with an error code and additional details.
     *
     * @param errorCode the error code
     * @param details   additional error information
     */
    public VocabException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage() + " - " + details);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * Creates a vocabulary exception with an error code, details, and a root cause.
     *
     * @param errorCode the error code
     * @param details   additional error information
     * @param cause     the underlying exception that caused this error
     */
    public VocabException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage() + " - " + details, cause);
        this.errorCode = errorCode;
        this.details = details;
    }
}
