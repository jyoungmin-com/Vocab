package jyoungmin.vocabcommons.security;

import jyoungmin.vocabcommons.exception.BaseServiceException;
import jyoungmin.vocabcommons.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for working with Spring Security Context.
 * Provides convenient methods for accessing authentication information.
 */
public class SecurityContextUtils {

    private SecurityContextUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Gets the current authenticated user's username.
     *
     * @return current username
     * @throws BaseServiceException if no authentication information is available
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new BaseServiceException(
                    ErrorCode.AUTHENTICATION_REQUIRED,
                    "No authentication info"
            );
        }
        return authentication.getName();
    }

    /**
     * Gets the current authentication principal without type checking.
     *
     * @param <T> expected principal type
     * @return current authentication principal
     * @throws BaseServiceException if no authentication information is available
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BaseServiceException(
                    ErrorCode.AUTHENTICATION_REQUIRED,
                    "No authentication info"
            );
        }
        return (T) authentication.getPrincipal();
    }

    /**
     * Gets the current authentication principal with type safety.
     * Validates that the principal matches the expected type.
     *
     * @param <T> expected principal type
     * @param principalClass expected class of the principal
     * @return current authentication principal
     * @throws BaseServiceException if no authentication information is available or type mismatch occurs
     */
    public static <T> T getCurrentPrincipal(Class<T> principalClass) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BaseServiceException(
                    ErrorCode.AUTHENTICATION_REQUIRED,
                    "No authentication info"
            );
        }

        Object principal = authentication.getPrincipal();
        if (!principalClass.isInstance(principal)) {
            throw new BaseServiceException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Principal type mismatch. Expected: " + principalClass.getName() +
                            ", Actual: " + principal.getClass().getName()
            );
        }

        return principalClass.cast(principal);
    }

    /**
     * Gets the current authentication object.
     *
     * @return current authentication or null if not authenticated
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Checks if the current user is authenticated.
     *
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal());
    }
}
