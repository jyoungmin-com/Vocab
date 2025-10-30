package jyoungmin.vocablist.util;

import jyoungmin.vocablist.dto.UserInfo;
import jyoungmin.vocablist.exception.ErrorCode;
import jyoungmin.vocablist.exception.VocabException;
import jyoungmin.vocablist.repository.ListRepository;
import jyoungmin.vocablist.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUser {
    private final WordRepository wordRepository;
    private final ListRepository listRepository;

    public UserInfo getUserInfo() {
        // SecurityContext에서 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new VocabException(
                    ErrorCode.INVALID_TOKEN,
                    "No authentication info"
            );
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserInfo)) {
            throw new VocabException(
                    ErrorCode.INVALID_TOKEN,
                    "Invalid authentication principal type"
            );
        }

        return (UserInfo) principal;
    }

    public boolean verifyWordOwner(long userId, long wordId) {
        if (wordRepository.findByIdAndUserId(wordId, userId).isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean verifyListOwner(long userId, long listId) {
        if (listRepository.findByIdAndUserId(listId, userId).isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
}
