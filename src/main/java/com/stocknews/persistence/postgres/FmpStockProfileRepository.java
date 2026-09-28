package com.stocknews.persistence.postgres;

import com.stocknews.persistence.FmpStockProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FmpStockProfileRepository extends JpaRepository<FmpStockProfile, Long> {
    List<FmpStockProfile> findAllBySymbolIn(Collection<String> symbols);
}
