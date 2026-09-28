package com.stocknews.persistence.postgres;

import com.stocknews.persistence.AlphaVantageOverview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AlphaVantageOverviewRepository extends JpaRepository<AlphaVantageOverview, Long> {
    List<AlphaVantageOverview> findAllBySymbolIn(Collection<String> symbols);
}
