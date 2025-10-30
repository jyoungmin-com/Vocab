package jyoungmin.vocabauth.service;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jyoungmin.vocabauth.dto.JwtToken;
import jyoungmin.vocabauth.dto.LoginRequest;
import jyoungmin.vocabauth.entity.User;
import jyoungmin.vocabauth.exception.AuthException;
import jyoungmin.vocabauth.repository.UserRepository;
import jyoungmin.vocabauth.security.JwtTokenProvider;
import jyoungmin.vocabauth.util.SecurityUtil;
import jyoungmin.vocabcommons.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Register a new user
     * @param user User information to register
     */
    @Transactional
    @RateLimiter(name = "auth-register")
    public void register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setEnabled(true);
        userRepository.save(user);

        log.info("User registered successfully: {}", user.getUserName());
    }

    /**
     * Authenticate user and generate JWT tokens
     * @param loginRequest Login credentials
     * @return JWT tokens (access + refresh)
     */
    @RateLimiter(name = "auth-login")
    public JwtToken login(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        log.info("User logged in successfully: {}", loginRequest.getUserName());
        return jwtToken;
    }

    /**
     * Logout user by deleting refresh token
     */
    public void logout() {
        String username = SecurityUtil.getCurrentUsername();
        jwtTokenProvider.deleteRefreshToken(username);

        log.info("User logged out successfully: {}", username);
    }

    /**
     * Check if username already exists
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    @RateLimiter(name = "auth-general")
    public boolean checkUsernameExists(String username) {
        return userRepository.findByUserName(username).isPresent();
    }

    /**
     * Get current authenticated user
     * @return User without password
     */
    @RateLimiter(name = "auth-general")
    public User getCurrentUser() {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AuthException(
                        ErrorCode.USER_NOT_FOUND,
                        "User '" + username + "' not found"
                ));

        user.setPassword(null); // Remove password from response
        return user;
    }

    /**
     * Refresh access token using refresh token
     * @param refreshToken Refresh token
     * @return New JWT tokens
     */
    @RateLimiter(name = "auth-general")
    public JwtToken refreshToken(String refreshToken) {
        // Validate refresh token
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new AuthException(
                    ErrorCode.REFRESH_TOKEN_INVALID,
                    "Refresh token is invalid or expired"
            );
        }

        // Extract username from refresh token
        String username = jwtTokenProvider.getUserNameFromToken(refreshToken);

        // Issue new AccessToken + RefreshToken
        JwtToken newToken = jwtTokenProvider.generateTokenWithRefreshToken(username);

        log.info("Token refreshed successfully for user: {}", username);
        return newToken;
    }
}
