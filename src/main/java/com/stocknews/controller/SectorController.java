package com.stocknews.controller;

import com.stocknews.dto.SectorRequest;
import com.stocknews.dto.SectorResponse;
import com.stocknews.service.SectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/sectors")
@Tag(name = "Sectors", description = "Manage stock sectors")
public class SectorController {

    private final SectorService sectorService;

    public SectorController(SectorService sectorService) {
        this.sectorService = sectorService;
    }

    @PostMapping
    @Operation(summary = "Create a sector")
    public ResponseEntity<SectorResponse> create(@Valid @RequestBody SectorRequest request) {
        SectorResponse created = sectorService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "List sectors")
    public List<SectorResponse> findAll() {
        return sectorService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a sector by ID")
    public SectorResponse findById(@PathVariable Integer id) {
        return sectorService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a sector")
    public SectorResponse update(@PathVariable Integer id, @Valid @RequestBody SectorRequest request) {
        return sectorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a sector")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        sectorService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
