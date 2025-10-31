package jyoungmin.vocabauth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

/**
 * Data transfer object for user login requests.
 * Contains credentials required for authentication.
 */
@Data
@Builder
public class LoginRequest {
    /**
     * Username for authentication
     */
    @NotBlank(message = "Username is required")
    private String userName;

    /**
     * Password for authentication
     */
    @NotBlank(message = "Password is required")
    private String password;
}