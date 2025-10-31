package jyoungmin.vocablist.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a vocabulary word.
 * Supports Japanese words with furigana readings.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Word {
    /**
     * Unique identifier for the word
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The vocabulary word (supports Japanese characters)
     */
    @Column(nullable = false, unique = false)
    private String word;

    /**
     * Whether the user has memorized this word
     */
    @Column(nullable = false)
    private boolean isMemorized = false;

    /**
     * Furigana reading for Japanese words (hiragana/katakana)
     */
    @Column(nullable = true)
    private String furigana;

    /**
     * Translation or meaning of the word
     */
    @Column(nullable = false)
    private String meaning;

    /**
     * ID of the user who owns this word
     */
    @Column(nullable = false)
    private long userId;

    /**
     * ID of the list containing this word
     */
    @Column(nullable = false)
    private long listId;

    /**
     * Timestamp when the word was created
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the word was last modified
     */
    private LocalDateTime modifiedAt;

    /**
     * Automatically sets creation and modification timestamps when entity is persisted.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

    /**
     * Automatically updates the modification timestamp when entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }
}
