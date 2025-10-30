package jyoungmin.vocablist.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Authentication & Authorization Errors (4001-4099)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_4001", "Invalid or expired token"),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "AUTH_4003", "You do not have permission to access this resource"),

    // Validation Errors (4100-4199)
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "VAL_4100", "Invalid input provided"),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "VAL_4101", "Required field is missing"),
    FURIGANA_REQUIRED(HttpStatus.BAD_REQUEST, "VAL_4102", "Furigana is required for Japanese words"),
    DUPLICATE_WORD(HttpStatus.CONFLICT, "VAL_4109", "Word already exists in your vocabulary"),

    // Resource Not Found Errors (4040-4049)
    WORD_NOT_FOUND(HttpStatus.NOT_FOUND, "RES_4040", "Word not found"),
    LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "RES_4041", "List not found"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "RES_4042", "User not found"),

    // Business Logic Errors (4200-4299)
    LIST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "BIZ_4200", "You do not own this list"),
    WORD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "BIZ_4201", "You do not own this word"),

    // Service Communication Errors (5020-5029)
    AUTH_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SVC_5020", "Authentication service is unavailable"),
    AUTH_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SVC_5021", "Authentication service error"),

    // Internal Server Errors (5000-5019)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_5000", "An unexpected error occurred"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_5001", "Database operation failed");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
