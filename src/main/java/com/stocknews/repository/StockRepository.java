package com.stocknews.repository;

import com.stocknews.model.Stock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Integer> {

    @Override
    @EntityGraph(attributePaths = "sector")
    List<Stock> findAll();

    @Override
    @EntityGraph(attributePaths = "sector")
    Optional<Stock> findById(Integer id);

    @EntityGraph(attributePaths = "sector")
    List<Stock> findBySectorId(Integer sectorId);

    @EntityGraph(attributePaths = "sector")
    List<Stock> findByTickerIgnoreCase(String ticker);

    @EntityGraph(attributePaths = "sector")
    List<Stock> findByTickerIgnoreCaseAndSectorId(String ticker, Integer sectorId);

    boolean existsBySectorId(Integer sectorId);

    boolean existsByTickerIgnoreCaseAndSectorId(String ticker, Integer sectorId);
}
