package jyoungmin.vocabauth.controller;

import jakarta.validation.Valid;
import jyoungmin.vocabauth.dto.LoginRequest;
import jyoungmin.vocabauth.dto.RefreshTokenRequest;
import jyoungmin.vocabauth.entity.User;
import jyoungmin.vocabauth.exception.AuthException;
import jyoungmin.vocabauth.exception.ErrorCode;
import jyoungmin.vocabauth.repository.UserRepository;
import jyoungmin.vocabauth.dto.JwtToken;
import jyoungmin.vocabauth.security.JwtTokenProvider;
import jyoungmin.vocabauth.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
@ResponseBody
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> register(@Valid @RequestBody User user) {
        // Validation will be done by database constraints
        // DataIntegrityViolationException will be caught by GlobalExceptionHandler
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@Valid @RequestBody LoginRequest loginRequest) {
        // BadCredentialsException will be caught by GlobalExceptionHandler
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        return ResponseEntity.ok(jwtToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        String username = SecurityUtil.getCurrentUsername();
        jwtTokenProvider.deleteRefreshToken(username);
        return ResponseEntity.ok("Logout successful");
    }

    @GetMapping("/duplicate/{username}")
    public ResponseEntity<Boolean> checkUsername(@PathVariable String username) {
        boolean exists = userRepository.findByUserName(username).isPresent();
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AuthException(
                        ErrorCode.USER_NOT_FOUND,
                        "User '" + username + "' not found"
                ));

        user.setPassword(null); // Remove password from response
        return ResponseEntity.ok(user);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtToken> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token (format + Redis stored value comparison)
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

        return ResponseEntity.ok(newToken);
    }

}
