package jyoungmin.vocabcommons.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jyoungmin.vocabcommons.exception.BaseServiceException;
import jyoungmin.vocabcommons.exception.ErrorCode;
import jyoungmin.vocabcommons.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Entry point for handling authentication failures in JWT-based security.
 * Returns consistent JSON error responses when authentication fails.
 * Extracts detailed error information from BaseServiceException if available.
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * JSON mapper for serializing error responses
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handles authentication failures and sends JSON error response.
     * Checks if the cause is a BaseServiceException for detailed error information.
     *
     * @param request       the HTTP request
     * @param response      the HTTP response
     * @param authException the authentication exception
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.warn("Authentication failed for request to {}: {}",
                request.getRequestURI(), authException.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_TOKEN;
        String errorMessage = "Authentication required. Please provide a valid token.";

        // Check if the cause is a BaseServiceException and extract error details
        if (authException.getCause() instanceof BaseServiceException) {
            BaseServiceException serviceException = (BaseServiceException) authException.getCause();
            errorCode = serviceException.getErrorCode();
            errorMessage = serviceException.getMessage();
        } else if (authException.getMessage() != null) {
            errorMessage = authException.getMessage();
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getHttpStatus().value());

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                errorMessage,
                request.getRequestURI()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
