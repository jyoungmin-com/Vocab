package jyoungmin.vocablist.security;

import feign.FeignException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jyoungmin.vocablist.client.AuthClient;
import jyoungmin.vocablist.dto.UserInfo;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthClient authClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                // VocabAuth 서비스에 토큰 검증 요청
                UserInfo userInfo = authClient.getAuthenticatedUser(authorizationHeader);

                // 인증 성공 시 SecurityContext에 저장
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userInfo,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(userInfo.getRole()))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (FeignException.Unauthorized e) {
                // VocabAuth에서 401 응답 (토큰 만료 또는 유효하지 않음)
                log.warn("Token validation failed - Unauthorized: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            } catch (FeignException e) {
                // 기타 Feign 에러 (VocabAuth 서비스 장애 등)
                log.error("Failed to communicate with VocabAuth service: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            } catch (Exception e) {
                // 예상치 못한 에러
                log.error("Unexpected error during token validation: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
