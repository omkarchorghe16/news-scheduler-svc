package com.stocknews.persistence.postgres;

import com.stocknews.persistence.AlphaVantageApiCall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AlphaVantageApiCallRepository extends JpaRepository<AlphaVantageApiCall, Long> {
    long countByCalledAtAfter(LocalDateTime since);

    Optional<AlphaVantageApiCall> findFirstByCalledAtAfterOrderByCalledAtAsc(LocalDateTime since);
}
