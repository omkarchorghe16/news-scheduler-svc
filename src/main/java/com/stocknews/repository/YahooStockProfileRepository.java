package com.stocknews.repository;

import com.stocknews.model.YahooStockProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface YahooStockProfileRepository extends JpaRepository<YahooStockProfile, Long> {
    List<YahooStockProfile> findAllBySymbolIn(Collection<String> symbols);
}
