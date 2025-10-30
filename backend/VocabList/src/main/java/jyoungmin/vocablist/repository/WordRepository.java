package jyoungmin.vocablist.repository;

import jyoungmin.vocablist.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {
    List<Word> findByWordAndUserId(String word, long userId);

    List<Word> findByIdAndUserId(long id, long userId);

    List<Word> getWordsByListId(long listId);

    List<Word> getWordsByUserId(long userId);

    Word getWordByidAndUserId(long id, long userId);
}
