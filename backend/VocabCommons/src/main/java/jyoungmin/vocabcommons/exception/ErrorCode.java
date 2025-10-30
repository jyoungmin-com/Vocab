package jyoungmin.vocabcommons.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ========== Authentication Errors (4010-4019) ==========
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_4010", "Invalid username or password"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_4011", "Token has expired"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_4012", "Invalid token"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_4013", "Refresh token not found"),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_4014", "Invalid or expired refresh token"),

    // ========== Authorization Errors (4030-4039) ==========
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "AUTH_4030", "You do not have permission to access this resource"),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "AUTH_4031", "Account is disabled"),

    // ========== Resource Not Found Errors (4040-4049) ==========
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_4040", "User not found"),
    WORD_NOT_FOUND(HttpStatus.NOT_FOUND, "RES_4040", "Word not found"),
    LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "RES_4041", "List not found"),

    // ========== User Management Errors (4090-4099) ==========
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_4090", "Username already exists"),
    INVALID_USER_DATA(HttpStatus.BAD_REQUEST, "USER_4091", "Invalid user data provided"),

    // ========== Validation Errors (4100-4109) ==========
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "VAL_4100", "Invalid input provided"),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "VAL_4101", "Required field is missing"),
    FURIGANA_REQUIRED(HttpStatus.BAD_REQUEST, "VAL_4102", "Furigana is required for Japanese words"),
    DUPLICATE_WORD(HttpStatus.CONFLICT, "VAL_4109", "Word already exists in your vocabulary"),

    // ========== Business Logic Errors (4200-4299) ==========
    LIST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "BIZ_4200", "You do not own this list"),
    WORD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "BIZ_4201", "You do not own this word"),

    // ========== Service Communication Errors (5020-5029) ==========
    AUTH_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SVC_5020", "Authentication service is unavailable"),
    AUTH_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SVC_5021", "Authentication service error"),

    // ========== Internal Server Errors (5000-5019) ==========
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_5000", "An unexpected error occurred"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_5001", "Database operation failed"),
    REDIS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_5002", "Redis operation failed");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
