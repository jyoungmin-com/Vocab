package jyoungmin.vocablist.util;

import jyoungmin.vocabcommons.dto.UserInfo;
import jyoungmin.vocabcommons.exception.ErrorCode;
import jyoungmin.vocablist.exception.VocabException;
import jyoungmin.vocablist.repository.ListRepository;
import jyoungmin.vocablist.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility for accessing authenticated user information and verifying resource ownership.
 * Extracts user details from Spring Security context and validates permissions.
 */
@Component
@RequiredArgsConstructor
public class AuthUser {
    /**
     * Repository for word data access
     */
    private final WordRepository wordRepository;

    /**
     * Repository for list data access
     */
    private final ListRepository listRepository;

    /**
     * Retrieves the currently authenticated user's information from SecurityContext.
     *
     * @return user information from authentication
     * @throws VocabException if not authenticated or invalid principal type
     */
    public UserInfo getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new VocabException(
                    ErrorCode.AUTHENTICATION_REQUIRED,
                    "No authentication info"
            );
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserInfo)) {
            throw new VocabException(
                    ErrorCode.AUTHENTICATION_REQUIRED,
                    "Invalid authentication principal type"
            );
        }

        return (UserInfo) principal;
    }

    /**
     * Verifies that a word belongs to the specified user.
     *
     * @param userId the user's ID
     * @param wordId the word ID to verify
     * @return true if user owns the word, false otherwise
     */
    public boolean verifyWordOwner(long userId, long wordId) {
        if (wordRepository.findByIdAndUserId(wordId, userId).isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Verifies that a list belongs to the specified user.
     *
     * @param userId the user's ID
     * @param listId the list ID to verify
     * @return true if user owns the list, false otherwise
     */
    public boolean verifyListOwner(long userId, long listId) {
        if (!listRepository.findByIdAndUserId(listId, userId).isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if a list exists in the database.
     *
     * @param listId the list ID to check
     * @return true if list exists, false otherwise
     */
    public boolean listExists(long listId) {
        return listRepository.existsById(listId);
    }
}
