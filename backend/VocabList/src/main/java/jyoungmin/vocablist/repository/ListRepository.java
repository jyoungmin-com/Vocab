package jyoungmin.vocablist.repository;

import jyoungmin.vocablist.entity.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ListRepository extends JpaRepository<List, Long> {
    Optional<List> findFirstByUserIdOrderByCreatedAtAsc(Long userId);

    java.util.List<List> findAllByUserId(Long userId);

    java.util.List<List> findByIdAndUserId(long id, long userId);
}
