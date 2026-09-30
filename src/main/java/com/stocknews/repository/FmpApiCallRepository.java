package com.stocknews.repository;

import com.stocknews.model.FmpApiCall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface FmpApiCallRepository extends JpaRepository<FmpApiCall, Long> {
    long countByCalledAtAfter(LocalDateTime since);
}
