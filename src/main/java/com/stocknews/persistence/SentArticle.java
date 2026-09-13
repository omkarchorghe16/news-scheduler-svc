package com.stocknews.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA entity representing a sent article.
 * Used for deduplication: stores SHA-256 hash of article URL and timestamp when sent.
 */
@Entity
@Table(name = "sent_articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String articleUrlHash;

    @Column(nullable = false)
    private LocalDateTime sentAt;
}
