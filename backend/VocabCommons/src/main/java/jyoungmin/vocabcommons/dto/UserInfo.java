package jyoungmin.vocabcommons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object containing authenticated user information.
 * Used for passing user data between services.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {
    /**
     * Unique identifier of the user
     */
    private Long id;

    /**
     * Username for authentication
     */
    private String userName;

    /**
     * Full name of the user
     */
    private String name;

    /**
     * Email address of the user
     */
    private String email;

    /**
     * Role assigned to the user
     */
    private String role;

    /**
     * Whether the user account is enabled
     */
    private boolean enabled;
}
