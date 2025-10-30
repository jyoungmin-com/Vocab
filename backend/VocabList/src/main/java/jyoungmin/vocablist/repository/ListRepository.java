package jyoungmin.vocablist.repository;

import jyoungmin.vocablist.entity.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ListRepository extends JpaRepository<List, Long> {
    // 사용자의 첫 번째 리스트 조회 (생성일 기준 오름차순)
    Optional<List> findFirstByUserIdOrderByCreatedAtAsc(Long userId);

    // 사용자의 모든 리스트 조회
    java.util.List<List> findAllByUserId(Long userId);

    java.util.List<List> findByIdAndUserId(long id, long userId);
}
