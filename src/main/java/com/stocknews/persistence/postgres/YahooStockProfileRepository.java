package com.stocknews.persistence.postgres;

import com.stocknews.persistence.YahooStockProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface YahooStockProfileRepository extends JpaRepository<YahooStockProfile, Long> {
    List<YahooStockProfile> findAllBySymbolIn(Collection<String> symbols);
}
