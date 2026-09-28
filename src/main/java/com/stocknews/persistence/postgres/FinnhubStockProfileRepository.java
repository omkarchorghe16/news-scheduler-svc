package com.stocknews.persistence.postgres;

import com.stocknews.persistence.FinnhubStockProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FinnhubStockProfileRepository extends JpaRepository<FinnhubStockProfile, Long> {
    List<FinnhubStockProfile> findAllBySymbolIn(Collection<String> symbols);
}
