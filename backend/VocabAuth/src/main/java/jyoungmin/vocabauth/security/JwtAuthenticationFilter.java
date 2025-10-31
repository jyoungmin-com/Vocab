package jyoungmin.vocabauth.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jyoungmin.vocabcommons.exception.BaseServiceException;
import jyoungmin.vocabcommons.exception.ErrorCode;
import jyoungmin.vocabcommons.security.JwtFilterUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * JWT authentication filter that validates tokens and establishes security context.
 * Processes JWT tokens from Authorization headers and sets authentication for valid requests.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilter {
    /**
     * Provider for JWT token validation and authentication extraction
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Filters incoming requests to validate JWT tokens and set authentication context.
     * Extracts JWT from Authorization header, validates it, and populates SecurityContext.
     *
     * @param servletRequest  the incoming request
     * @param servletResponse the outgoing response
     * @param filterChain     the filter chain to continue processing
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            // Get JWT token from request header
            String accessToken = resolveToken(request);

            // Validate and set authentication
            if (accessToken != null) {
                if (jwtTokenProvider.validateToken(accessToken)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Add username to MDC for logging
                    String username = authentication.getName();
                    JwtFilterUtils.addUserToMDC(username);
                } else {
                    SecurityContextHolder.clearContext();
                }
            }

            filterChain.doFilter(servletRequest, servletResponse);

        } catch (BaseServiceException e) {
            log.warn("BaseServiceException in JWT filter: {}", e.getMessage());
            JwtFilterUtils.sendErrorResponse(response, e.getErrorCode(), request.getRequestURI());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid JWT token format: {}", e.getMessage());
            JwtFilterUtils.sendErrorResponse(response, ErrorCode.INVALID_TOKEN, request.getRequestURI());
        } catch (Exception e) {
            log.error("Unexpected error in JWT filter: {}", e.getMessage(), e);
            JwtFilterUtils.sendErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI());
        }
    }

    /**
     * Extracts JWT token from the Authorization header.
     * Expects format: "Bearer {token}"
     *
     * @param request the HTTP request containing the Authorization header
     * @return the JWT token without "Bearer " prefix, or null if not present
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
