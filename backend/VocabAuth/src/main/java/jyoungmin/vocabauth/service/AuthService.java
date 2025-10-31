package jyoungmin.vocabauth.service;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jyoungmin.vocabauth.dto.JwtToken;
import jyoungmin.vocabauth.dto.LoginRequest;
import jyoungmin.vocabauth.entity.User;
import jyoungmin.vocabauth.exception.AuthException;
import jyoungmin.vocabauth.repository.UserRepository;
import jyoungmin.vocabauth.security.JwtTokenProvider;
import jyoungmin.vocabcommons.exception.ErrorCode;
import jyoungmin.vocabcommons.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling authentication operations.
 * Manages user registration, login, logout, token refresh, and user information retrieval.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    /**
     * Repository for user data access
     */
    private final UserRepository userRepository;

    /**
     * Encoder for password hashing and validation
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Spring Security's authentication manager
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Provider for JWT token operations
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Registers a new user with encoded password and default role.
     *
     * @param user the user information to register
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
     * Authenticates user credentials and generates JWT token pair.
     * Verifies account is enabled before issuing tokens.
     *
     * @param loginRequest the login credentials
     * @return JWT token pair containing access and refresh tokens
     * @throws AuthException if user not found or account is disabled
     */
    @RateLimiter(name = "auth-login")
    public JwtToken login(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // Check if account is enabled
        User user = userRepository.findByUserName(loginRequest.getUserName())
                .orElseThrow(() -> new AuthException(
                        ErrorCode.USER_NOT_FOUND,
                        "User '" + loginRequest.getUserName() + "' not found"
                ));

        if (!user.isEnabled()) {
            throw new AuthException(
                    ErrorCode.ACCOUNT_DISABLED,
                    "Your account has been disabled. Please contact support."
            );
        }

        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        log.info("User logged in successfully: {}", loginRequest.getUserName());
        return jwtToken;
    }

    /**
     * Logs out the current user by removing their refresh token from storage.
     * Prevents further token refreshing until next login.
     */
    public void logout() {
        String username = SecurityContextUtils.getCurrentUsername();
        jwtTokenProvider.deleteRefreshToken(username);

        log.info("User logged out successfully: {}", username);
    }

    /**
     * Checks if a username is already registered in the system.
     *
     * @param username the username to verify
     * @return true if username exists, false otherwise
     */
    @RateLimiter(name = "auth-general")
    public boolean checkUsernameExists(String username) {
        return userRepository.findByUserName(username).isPresent();
    }

    /**
     * Retrieves the currently authenticated user's information.
     * Removes password from response for security.
     *
     * @return user information without sensitive data
     * @throws AuthException if user not found
     */
    @RateLimiter(name = "auth-general")
    public User getCurrentUser() {
        String username = SecurityContextUtils.getCurrentUsername();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AuthException(
                        ErrorCode.USER_NOT_FOUND,
                        "User '" + username + "' not found"
                ));

        user.setPassword(null); // Remove password from response
        return user;
    }

    /**
     * Generates new token pair using a valid refresh token.
     * Validates the refresh token before issuing new tokens.
     *
     * @param refreshToken the refresh token to validate
     * @return new JWT token pair with updated expiration times
     * @throws AuthException if refresh token is invalid or not found
     */
    @RateLimiter(name = "auth-general")
    public JwtToken refreshToken(String refreshToken) {
        // Validate refresh token
        jwtTokenProvider.validateRefreshToken(refreshToken);

        // Extract username from refresh token
        String username = jwtTokenProvider.getUserNameFromToken(refreshToken);

        // Issue new AccessToken + RefreshToken
        JwtToken newToken = jwtTokenProvider.generateTokenWithRefreshToken(username);

        log.info("Token refreshed successfully for user: {}", username);
        return newToken;
    }
}
