package jyoungmin.vocablist.service;

import jyoungmin.vocablist.dto.UserInfo;
import jyoungmin.vocablist.entity.Word;
import jyoungmin.vocabcommons.exception.ErrorCode;
import jyoungmin.vocablist.exception.VocabException;
import jyoungmin.vocablist.repository.WordRepository;
import jyoungmin.vocablist.util.AuthUser;
import jyoungmin.vocablist.util.JapaneseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WordServiceTest {

    @Mock
    private WordRepository wordRepository;

    @Mock
    private AuthUser authUser;

    @Mock
    private ListService listService;

    @Mock
    private JapaneseValidator japaneseValidator;

    @InjectMocks
    private WordService wordService;

    private Long userId;
    private Long wordId;
    private Word mockWord;
    private UserInfo mockUserInfo;

    @BeforeEach
    void setUp() {
        userId = 1L;
        wordId = 100L;

        mockUserInfo = UserInfo.builder()
                .id(userId)
                .userName("testuser")
                .build();

        mockWord = Word.builder()
                .id(wordId)
                .word("テスト")
                .meaning("test")
                .furigana("てすと")
                .userId(userId)
                .listId(1L)
                .isMemorized(false)
                .build();
    }

    @Test
    @DisplayName("단어 삭제 - 존재하는 단어를 삭제하면 true 반환")
    void deleteWordById_WhenWordExists_ShouldReturnTrue() {
        // given
        when(authUser.getUserInfo()).thenReturn(mockUserInfo);
        when(wordRepository.getWordByidAndUserId(wordId, userId))
                .thenReturn(mockWord);

        // when
        boolean result = wordService.deleteWordById(wordId);

        // then
        assertTrue(result);
        verify(wordRepository, times(1)).deleteById(wordId);
    }

    @Test
    @DisplayName("단어 삭제 - 존재하지 않는 단어를 삭제하면 예외 발생")
    void deleteWordById_WhenWordDoesNotExist_ShouldThrowException() {
        // given
        when(authUser.getUserInfo()).thenReturn(mockUserInfo);
        when(wordRepository.getWordByidAndUserId(wordId, userId))
                .thenReturn(null);

        // when & then
        VocabException exception = assertThrows(VocabException.class,
                () -> wordService.deleteWordById(wordId));

        assertEquals(ErrorCode.WORD_NOT_FOUND, exception.getErrorCode());
        assertEquals("Word not found", exception.getMessage());
        verify(wordRepository, never()).deleteById(wordId);
    }

    @Test
    @DisplayName("단어 삭제 - 다른 사용자의 단어를 삭제하려고 하면 예외 발생")
    void deleteWordById_WhenWordBelongsToAnotherUser_ShouldThrowException() {
        // given
        Long otherUserId = 999L;
        UserInfo otherUserInfo = UserInfo.builder()
                .id(otherUserId)
                .userName("otheruser")
                .build();

        when(authUser.getUserInfo()).thenReturn(otherUserInfo);
        when(wordRepository.getWordByidAndUserId(wordId, otherUserId))
                .thenReturn(null);

        // when & then
        VocabException exception = assertThrows(VocabException.class,
                () -> wordService.deleteWordById(wordId));

        assertEquals(ErrorCode.WORD_NOT_FOUND, exception.getErrorCode());
        verify(wordRepository, never()).deleteById(wordId);
    }
}
