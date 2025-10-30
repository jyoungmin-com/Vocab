package jyoungmin.vocabauth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Authentication Errors (4010-4019)
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_4010", "Invalid username or password"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_4011", "Token has expired"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_4012", "Invalid token"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_4013", "Refresh token not found"),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_4014", "Invalid or expired refresh token"),

    // Authorization Errors (4030-4039)
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "AUTH_4030", "You do not have permission to access this resource"),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "AUTH_4031", "Account is disabled"),

    // User Management Errors (4090-4099)
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_4090", "Username already exists"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_4040", "User not found"),
    INVALID_USER_DATA(HttpStatus.BAD_REQUEST, "USER_4091", "Invalid user data provided"),

    // Validation Errors (4100-4109)
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "VAL_4100", "Invalid input provided"),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "VAL_4101", "Required field is missing"),

    // Internal Server Errors (5000-5019)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_5000", "An unexpected error occurred"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_5001", "Database operation failed"),
    REDIS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_5002", "Redis operation failed");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
