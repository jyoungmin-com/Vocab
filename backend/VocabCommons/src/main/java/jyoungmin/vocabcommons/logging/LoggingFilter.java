package jyoungmin.vocabcommons.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jyoungmin.vocabcommons.constants.LoggingConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Filter for logging HTTP requests and responses with correlation ID tracking.
 * Generates or propagates correlation IDs, logs request/response details, and masks sensitive data.
 * Runs with the highest precedence to ensure all requests are logged.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter implements Filter {

    /**
     * Maximum length of request body to log (1KB)
     */
    private static final int MAX_BODY_LENGTH = 1000;

    /**
     * Set of sensitive field names to mask in logs
     */
    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList(
            "password", "token", "accessToken", "refreshToken", "authorization"
    ));

    /**
     * JSON mapper for parsing and masking request bodies
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Filters requests and responses to log details with correlation ID.
     * Wraps request/response for body capture and ensures MDC cleanup.
     *
     * @param request  the servlet request
     * @param response the servlet response
     * @param chain    the filter chain
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Generate or extract correlation ID
        String correlationId = httpRequest.getHeader(LoggingConstants.CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Add correlation ID to MDC for logging
        MDC.put(LoggingConstants.CORRELATION_ID_LOG_KEY, correlationId);

        // Add correlation ID to response header
        httpResponse.setHeader(LoggingConstants.CORRELATION_ID_HEADER, correlationId);

        // Wrap request and response for logging
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();

        try {
            // Log incoming request
            logRequest(wrappedRequest, correlationId);

            // Continue filter chain
            chain.doFilter(wrappedRequest, wrappedResponse);

            // Log outgoing response
            logResponse(wrappedRequest, wrappedResponse, startTime, correlationId);

        } finally {
            // Copy body to response
            wrappedResponse.copyBodyToResponse();

            // Clear MDC
            MDC.clear();
        }
    }

    /**
     * Logs incoming request details including method, URI, user, and masked body.
     *
     * @param request       the wrapped request
     * @param correlationId the correlation ID for this request
     */
    private void logRequest(ContentCachingRequestWrapper request, String correlationId) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUri = queryString != null ? uri + "?" + queryString : uri;
        String contentType = request.getContentType();

        // Get username from MDC
        String username = MDC.get(LoggingConstants.USERNAME_LOG_KEY);
        String userInfo = username != null ? " | User: " + username : "";

        // Get request body
        String requestBody = getRequestBody(request);
        String bodyInfo = requestBody != null && !requestBody.isEmpty()
                ? " | Body: " + requestBody
                : "";

        log.info("[REQUEST] {} {}{}{} | Content-Type: {} | Correlation-ID: {} | Remote-IP: {}",
                method, fullUri, userInfo, bodyInfo, contentType, correlationId, request.getRemoteAddr());
    }

    /**
     * Logs outgoing response details including status and duration.
     *
     * @param request       the wrapped request
     * @param response      the wrapped response
     * @param startTime     the request start time in milliseconds
     * @param correlationId the correlation ID for this request
     */
    private void logResponse(ContentCachingRequestWrapper request,
                             ContentCachingResponseWrapper response,
                             long startTime,
                             String correlationId) {
        long duration = System.currentTimeMillis() - startTime;
        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // Get username from MDC
        String username = MDC.get(LoggingConstants.USERNAME_LOG_KEY);
        String userInfo = username != null ? " | User: " + username : "";

        log.info("[RESPONSE] {} {}{} | Status: {} | Duration: {}ms | Correlation-ID: {}",
                method, uri, userInfo, status, duration, correlationId);
    }

    /**
     * Extracts and masks sensitive data from request body.
     * Limits body size to MAX_BODY_LENGTH and masks sensitive fields.
     *
     * @param request the wrapped request
     * @return masked request body or null if empty/error
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        try {
            byte[] content = request.getContentAsByteArray();
            if (content.length == 0) {
                return null;
            }

            // Limit body size for logging
            int length = Math.min(content.length, MAX_BODY_LENGTH);
            String body = new String(content, 0, length, StandardCharsets.UTF_8);

            // If content is truncated, add indicator
            if (content.length > MAX_BODY_LENGTH) {
                body += "... (truncated)";
            }

            // Try to parse as JSON and mask sensitive fields
            return maskSensitiveData(body);

        } catch (Exception e) {
            log.warn("Failed to read request body: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Masks sensitive fields in JSON body by replacing values with "***".
     * Returns original body if not valid JSON.
     *
     * @param body the request body to mask
     * @return masked body with sensitive fields hidden
     */
    private String maskSensitiveData(String body) {
        try {
            JsonNode jsonNode = objectMapper.readTree(body);
            if (jsonNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) jsonNode;
                SENSITIVE_FIELDS.forEach(field -> {
                    if (objectNode.has(field)) {
                        objectNode.put(field, "***");
                    }
                });
                return objectMapper.writeValueAsString(objectNode);
            }
            return body;
        } catch (Exception e) {
            // If not JSON, return as-is
            log.warn("Failed to mask sensitive data: {}", e.getMessage());
            return body;
        }
    }
}
