package jyoungmin.vocablist.security;

import feign.FeignException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jyoungmin.vocabcommons.dto.UserInfo;
import jyoungmin.vocabcommons.exception.ErrorCode;
import jyoungmin.vocabcommons.security.JwtFilterUtils;
import jyoungmin.vocablist.client.AuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT authentication filter for the vocabulary list service.
 * Validates tokens by calling the authentication service and establishes security context.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Feign client for communicating with the authentication service
     */
    private final AuthClient authClient;

    /**
     * Filters incoming requests to validate JWT tokens via the auth service.
     * Extracts user information and populates SecurityContext on successful validation.
     *
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                // Request token validation from VocabAuth service
                UserInfo userInfo = authClient.getAuthenticatedUser(authorizationHeader);

                // Store authentication in SecurityContext on success
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userInfo,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(userInfo.getRole()))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Add username to MDC for logging
                JwtFilterUtils.addUserToMDC(userInfo.getUserName());

            } catch (FeignException.Unauthorized e) {
                // 401 response from VocabAuth (token expired or invalid)
                log.warn("Token validation failed - Unauthorized: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                JwtFilterUtils.sendErrorResponse(response, ErrorCode.INVALID_TOKEN, request.getRequestURI());
                return;
            } catch (FeignException.ServiceUnavailable | FeignException.InternalServerError e) {
                // VocabAuth service down or internal error
                log.error("VocabAuth service unavailable: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                JwtFilterUtils.sendErrorResponse(response, ErrorCode.AUTH_SERVICE_UNAVAILABLE, request.getRequestURI());
                return;
            } catch (FeignException e) {
                // Other Feign communication errors
                log.error("Failed to communicate with VocabAuth service: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                JwtFilterUtils.sendErrorResponse(response, ErrorCode.AUTH_SERVICE_ERROR, request.getRequestURI());
                return;
            } catch (Exception e) {
                // Unexpected errors
                log.error("Unexpected error during token validation: {}", e.getMessage(), e);
                SecurityContextHolder.clearContext();
                JwtFilterUtils.sendErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
