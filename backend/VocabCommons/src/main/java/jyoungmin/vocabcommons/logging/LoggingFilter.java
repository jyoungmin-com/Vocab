package jyoungmin.vocabcommons.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_LOG_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Generate or extract correlation ID
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Add correlation ID to MDC for logging
        MDC.put(CORRELATION_ID_LOG_KEY, correlationId);

        // Add correlation ID to response header
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

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

    private void logRequest(ContentCachingRequestWrapper request, String correlationId) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUri = queryString != null ? uri + "?" + queryString : uri;

        log.info("[REQUEST] {} {} | Correlation-ID: {} | Remote-IP: {}",
                method, fullUri, correlationId, request.getRemoteAddr());
    }

    private void logResponse(ContentCachingRequestWrapper request,
                              ContentCachingResponseWrapper response,
                              long startTime,
                              String correlationId) {
        long duration = System.currentTimeMillis() - startTime;
        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        log.info("[RESPONSE] {} {} | Status: {} | Duration: {}ms | Correlation-ID: {}",
                method, uri, status, duration, correlationId);
    }
}
