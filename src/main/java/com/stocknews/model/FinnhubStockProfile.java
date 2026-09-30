package com.stocknews.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "finnhub_stock_profiles")
@Getter
@Setter
@NoArgsConstructor
public class FinnhubStockProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    private String name;
    private String country;
    private String currency;
    private String exchange;
    private String ipoDate;

    // Finnhub reports market capitalization and shares outstanding in millions.
    @Column(precision = 24, scale = 6)
    private BigDecimal marketCapitalization;

    @Column(precision = 24, scale = 6)
    private BigDecimal shareOutstanding;

    private String logo;
    private String phone;
    private String website;
    private String industry;

    @Column(columnDefinition = "TEXT")
    private String rawPayload;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }
}
