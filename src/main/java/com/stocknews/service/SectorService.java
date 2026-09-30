package com.stocknews.service;

import com.stocknews.dto.SectorRequest;
import com.stocknews.dto.SectorResponse;
import com.stocknews.exception.DuplicateResourceException;
import com.stocknews.exception.ResourceNotFoundException;
import com.stocknews.model.Sector;
import com.stocknews.repository.SectorRepository;
import com.stocknews.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SectorService {

    private final SectorRepository sectorRepository;
    private final StockRepository stockRepository;

    public SectorService(SectorRepository sectorRepository, StockRepository stockRepository) {
        this.sectorRepository = sectorRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public SectorResponse create(SectorRequest request) {
        String name = request.sectorName().trim();
        if (sectorRepository.existsBySectorNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Sector already exists: " + name);
        }
        Sector sector = new Sector();
        sector.setSectorName(name);
        return SectorResponse.from(sectorRepository.save(sector));
    }

    @Transactional(readOnly = true)
    public List<SectorResponse> findAll() {
        return sectorRepository.findAll().stream().map(SectorResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SectorResponse findById(Integer id) {
        return SectorResponse.from(getOrThrow(id));
    }

    @Transactional
    public SectorResponse update(Integer id, SectorRequest request) {
        Sector sector = getOrThrow(id);
        String name = request.sectorName().trim();
        sectorRepository.findBySectorNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Sector already exists: " + name);
                });
        sector.setSectorName(name);
        return SectorResponse.from(sectorRepository.save(sector));
    }

    @Transactional
    public void delete(Integer id) {
        Sector sector = getOrThrow(id);
        if (stockRepository.existsBySectorId(id)) {
            throw new DuplicateResourceException(
                    "Sector '" + sector.getSectorName() + "' still has stocks. Delete or move them first.");
        }
        sectorRepository.delete(sector);
    }

    private Sector getOrThrow(Integer id) {
        return sectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sector not found: " + id));
    }
}
