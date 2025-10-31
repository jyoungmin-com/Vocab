package jyoungmin.vocablist.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a vocabulary list.
 * Each list belongs to a user and contains multiple words.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class List {
    /**
     * Unique identifier for the list
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Name of the vocabulary list
     */
    @Column(nullable = false)
    private String listName;

    /**
     * ID of the user who owns this list
     */
    @Column(nullable = false)
    private long userId;

    /**
     * Timestamp when the list was created
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the list was last modified
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
