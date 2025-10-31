package jyoungmin.vocabauth.security;

import jyoungmin.vocabauth.entity.User;
import jyoungmin.vocabauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom implementation of UserDetailsService for loading user authentication details.
 * Retrieves user information from the database and converts it to Spring Security's UserDetails format.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Repository for accessing user data
     */
    private final UserRepository userRepository;

    /**
     * Loads user details by username for authentication.
     * Converts the User entity to Spring Security's UserDetails with authorities.
     *
     * @param username the username to load
     * @return UserDetails containing user information and authorities
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserName())
                .password(user.getPassword())
                .authorities(getAuthorities(user.getRole()))
                .disabled(!user.isEnabled())
                .build();
    }

    /**
     * Converts a role string to Spring Security authorities.
     * Prefixes the role with "ROLE_" as per Spring Security convention.
     *
     * @param role the user's role
     * @return collection of granted authorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }
}