package jyoungmin.vocablist.service;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jyoungmin.vocablist.entity.List;
import jyoungmin.vocablist.repository.ListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListService {

    private final ListRepository listRepository;

    /**
     * 사용자의 기본 리스트를 가져오거나 없으면 생성
     * 처음 로그인하는 사용자에게 자동으로 기본 단어장 생성
     *
     * @param userId 사용자 ID
     * @return 사용자의 첫 번째 리스트 (또는 새로 생성된 리스트)
     */
    @Transactional
    public List getOrCreateDefaultList(Long userId) {
        // 사용자의 첫 번째 리스트 조회
        return listRepository.findFirstByUserIdOrderByCreatedAtAsc(userId)
                .orElseGet(() -> {
                    // 리스트가 없으면 기본 리스트 생성
                    log.info("Creating default list for user: {}", userId);
                    List defaultList = List.builder()
                            .listName("Default")
                            .userId(userId)
                            .build();
                    return listRepository.save(defaultList);
                });
    }

    /**
     * 사용자의 모든 리스트 조회
     *
     * @param userId 사용자 ID
     * @return 사용자의 모든 리스트
     */
    @RateLimiter(name = "list-general")
    public java.util.List<List> getAllListsByUserId(Long userId) {
        return listRepository.findAllByUserId(userId);
    }

    /**
     * 새로운 리스트 생성
     *
     * @param userId   사용자 ID
     * @param listName 리스트 이름
     * @return 생성된 리스트
     */
    @Transactional
    @RateLimiter(name = "list-general")
    public List createList(Long userId, String listName) {
        List newList = List.builder()
                .listName(listName)
                .userId(userId)
                .build();
        return listRepository.save(newList);
    }
}
