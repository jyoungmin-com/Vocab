package jyoungmin.vocablist.util;

import jyoungmin.vocablist.entity.List;
import jyoungmin.vocablist.repository.ListRepository;
import jyoungmin.vocablist.repository.WordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUserTest {

    @Mock
    private WordRepository wordRepository;

    @Mock
    private ListRepository listRepository;

    @InjectMocks
    private AuthUser authUser;

    private Long userId;
    private Long listId;
    private List mockList;

    @BeforeEach
    void setUp() {
        userId = 1L;
        listId = 100L;

        mockList = List.builder()
                .id(listId)
                .listName("Test List")
                .userId(userId)
                .build();
    }

    @Test
    @DisplayName("리스트 소유자 검증 - 소유자가 맞을 때 true 반환")
    void verifyListOwner_WhenUserOwnsTheList_ShouldReturnTrue() {
        // given
        java.util.List<List> lists = new ArrayList<>();
        lists.add(mockList);
        when(listRepository.findByIdAndUserId(listId, userId))
                .thenReturn(lists);

        // when
        boolean result = authUser.verifyListOwner(userId, listId);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("리스트 소유자 검증 - 소유자가 아닐 때 false 반환")
    void verifyListOwner_WhenUserDoesNotOwnTheList_ShouldReturnFalse() {
        // given
        Long otherUserId = 999L;
        when(listRepository.findByIdAndUserId(listId, otherUserId))
                .thenReturn(new ArrayList<>());

        // when
        boolean result = authUser.verifyListOwner(otherUserId, listId);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("리스트 소유자 검증 - 존재하지 않는 리스트일 때 false 반환")
    void verifyListOwner_WhenListDoesNotExist_ShouldReturnFalse() {
        // given
        Long nonExistentListId = 999L;
        when(listRepository.findByIdAndUserId(nonExistentListId, userId))
                .thenReturn(new ArrayList<>());

        // when
        boolean result = authUser.verifyListOwner(userId, nonExistentListId);

        // then
        assertFalse(result);
    }
}
