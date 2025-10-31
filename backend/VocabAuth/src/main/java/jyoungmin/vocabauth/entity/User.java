package jyoungmin.vocabauth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stores user credentials, profile information, and account status.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Unique identifier for the user
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Unique username for authentication
     */
    @NotBlank(message = "Username is required")
    @Column(nullable = false, unique = true)
    private String userName;

    /**
     * User's full name
     */
    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    /**
     * Hashed password for authentication
     */
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    /**
     * User's email address
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false)
    private String email;

    /**
     * User's role for authorization (e.g., "USER", "ADMIN")
     */
    @Column(nullable = false)
    private String role;

    /**
     * Whether the account is enabled and can authenticate
     */
    @Column(nullable = false)
    private boolean enabled;
}
