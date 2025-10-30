package jyoungmin.vocablist.exception;

import lombok.Getter;

@Getter
public class VocabException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String details;

    public VocabException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public VocabException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }

    public VocabException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = details;
    }
}
