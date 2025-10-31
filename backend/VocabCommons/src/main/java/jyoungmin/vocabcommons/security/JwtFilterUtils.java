package jyoungmin.vocabcommons.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jyoungmin.vocabcommons.constants.LoggingConstants;
import jyoungmin.vocabcommons.exception.ErrorCode;
import jyoungmin.vocabcommons.exception.ErrorResponse;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Utility class for common JWT filter operations.
 * Provides methods for sending error responses, managing MDC, and extracting tokens.
 */
public class JwtFilterUtils {

    /**
     * JSON mapper for serializing error responses
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JwtFilterUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Sends a JSON error response with the given error code.
     *
     * @param response  the HTTP response
     * @param errorCode the error code to send
     * @param path      the request path
     * @throws IOException if an I/O error occurs
     */
    public static void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode, String path) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorCode.getHttpStatus().value());

        ErrorResponse errorResponse = ErrorResponse.of(errorCode, null, path);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Adds username to MDC (Mapped Diagnostic Context) for logging.
     * Allows log messages to include the authenticated user's username.
     *
     * @param username the username to add to MDC
     */
    public static void addUserToMDC(String username) {
        if (StringUtils.hasText(username)) {
            MDC.put(LoggingConstants.USERNAME_LOG_KEY, username);
        }
    }

    /**
     * Extracts JWT token from Authorization header.
     * Expects format: "Bearer {token}"
     *
     * @param authorizationHeader the Authorization header value
     * @return the JWT token without "Bearer " prefix, or null if not present
     */
    public static String resolveToken(String authorizationHeader) {
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
