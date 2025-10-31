package jyoungmin.vocabauth.controller;

import jakarta.validation.Valid;
import jyoungmin.vocabauth.dto.LoginRequest;
import jyoungmin.vocabauth.dto.RefreshTokenRequest;
import jyoungmin.vocabauth.entity.User;
import jyoungmin.vocabauth.dto.JwtToken;
import jyoungmin.vocabauth.service.AuthService;
import jyoungmin.vocabcommons.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * Handles user registration, login, logout, token refresh, and user information retrieval.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    /**
     * Service layer for authentication business logic
     */
    private final AuthService authService;

    /**
     * Registers a new user account.
     *
     * @param user the user registration information
     * @return response indicating successful registration
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody User user) {
        authService.register(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, ApiResponse.Messages.REGISTER_SUCCESS));
    }

    /**
     * Authenticates a user and returns JWT tokens.
     *
     * @param loginRequest the login credentials
     * @return response containing access and refresh tokens
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtToken>> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtToken jwtToken = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(ApiResponse.Messages.LOGIN_SUCCESS, jwtToken));
    }

    /**
     * Logs out the current user by invalidating their refresh token.
     *
     * @return response indicating successful logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success(ApiResponse.Messages.LOGOUT_SUCCESS));
    }

    /**
     * Checks if a username is already taken.
     *
     * @param username the username to check
     * @return response indicating whether the username exists
     */
    @GetMapping("/duplicate/{username}")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@PathVariable String username) {
        boolean exists = authService.checkUsernameExists(username);
        String message = exists ? ApiResponse.Messages.USERNAME_TAKEN : ApiResponse.Messages.USERNAME_AVAILABLE;
        return ResponseEntity.ok(ApiResponse.success(message, exists));
    }

    /**
     * Retrieves the currently authenticated user's information.
     *
     * @return response containing the user's profile data
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser() {
        User user = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(ApiResponse.Messages.USER_INFO_RETRIEVED, user));
    }

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param request the refresh token request
     * @return response containing new access and refresh tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtToken>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        JwtToken newToken = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(ApiResponse.Messages.TOKEN_REFRESHED, newToken));
    }
}
