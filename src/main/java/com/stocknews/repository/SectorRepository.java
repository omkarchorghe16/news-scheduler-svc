package com.stocknews.repository;

import com.stocknews.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SectorRepository extends JpaRepository<Sector, Integer> {

    boolean existsBySectorNameIgnoreCase(String sectorName);

    Optional<Sector> findBySectorNameIgnoreCase(String sectorName);
}
