package jyoungmin.vocablist.service;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jyoungmin.vocablist.entity.List;
import jyoungmin.vocablist.repository.ListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing vocabulary lists.
 * Handles list creation, retrieval, and default list management for users.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListService {

    /**
     * Repository for list data access
     */
    private final ListRepository listRepository;

    /**
     * Gets or creates a default list for the user.
     * Automatically creates a default vocabulary list for first-time users.
     *
     * @param userId the user's ID
     * @return the user's first list or newly created default list
     */
    @Transactional
    public List getOrCreateDefaultList(Long userId) {
        return listRepository.findFirstByUserIdOrderByCreatedAtAsc(userId)
                .orElseGet(() -> {
                    log.info("Creating default list for user: {}", userId);
                    List defaultList = List.builder()
                            .listName("Default")
                            .userId(userId)
                            .build();
                    return listRepository.save(defaultList);
                });
    }

    /**
     * Retrieves all lists belonging to a user.
     *
     * @param userId the user's ID
     * @return list of all user's vocabulary lists
     */
    @RateLimiter(name = "list-general")
    public java.util.List<List> getAllListsByUserId(Long userId) {
        return listRepository.findAllByUserId(userId);
    }

    /**
     * Creates a new vocabulary list for the user.
     *
     * @param userId   the user's ID
     * @param listName the name for the new list
     * @return the created list
     */
    @Transactional
    @RateLimiter(name = "list-general")
    public List createList(Long userId, String listName) {
        List newList = List.builder()
                .listName(listName)
                .userId(userId)
                .build();
        List savedList = listRepository.save(newList);
        log.info("List created: id={}, listName='{}', userId={}", savedList.getId(), savedList.getListName(), userId);
        return savedList;
    }
}
