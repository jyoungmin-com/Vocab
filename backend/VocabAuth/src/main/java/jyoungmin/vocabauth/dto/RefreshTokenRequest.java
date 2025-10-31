package jyoungmin.vocabauth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for token refresh requests.
 * Contains the refresh token to validate and exchange for new tokens.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {
    /**
     * Refresh token to validate and exchange for new access token
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
