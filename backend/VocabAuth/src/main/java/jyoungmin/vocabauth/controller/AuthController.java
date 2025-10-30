package jyoungmin.vocabauth.controller;

import jakarta.validation.Valid;
import jyoungmin.vocabauth.dto.LoginRequest;
import jyoungmin.vocabauth.dto.RefreshTokenRequest;
import jyoungmin.vocabauth.entity.User;
import jyoungmin.vocabauth.dto.JwtToken;
import jyoungmin.vocabauth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> register(@Valid @RequestBody User user) {
        authService.register(user);
        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtToken jwtToken = authService.login(loginRequest);
        return ResponseEntity.ok(jwtToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        authService.logout();
        return ResponseEntity.ok("Logout successful");
    }

    @GetMapping("/duplicate/{username}")
    public ResponseEntity<Boolean> checkUsername(@PathVariable String username) {
        boolean exists = authService.checkUsernameExists(username);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        User user = authService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtToken> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        JwtToken newToken = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(newToken);
    }
}
