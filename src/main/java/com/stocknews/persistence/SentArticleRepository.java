package com.stocknews.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for SentArticle persistence operations.
 */
@Repository
public interface SentArticleRepository extends JpaRepository<SentArticle, Long> {
    Optional<SentArticle> findByArticleUrlHash(String urlHash);

    long deleteAllBySentAtBefore(LocalDateTime cutoffDate);
}
