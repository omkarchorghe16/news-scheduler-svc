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
@Table(name = "alpha_vantage_overviews")
@Getter
@Setter
@NoArgsConstructor
public class AlphaVantageOverview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    private String name;
    private String exchange;
    private String currency;
    private String country;
    private String sector;
    private String industry;

    @Column(precision = 30, scale = 6)
    private BigDecimal marketCapitalization;

    @Column(precision = 30, scale = 6)
    private BigDecimal ebitda;

    @Column(precision = 24, scale = 8)
    private BigDecimal peRatio;
    @Column(precision = 24, scale = 8)
    private BigDecimal pegRatio;
    @Column(precision = 24, scale = 8)
    private BigDecimal bookValue;
    @Column(precision = 24, scale = 8)
    private BigDecimal dividendPerShare;
    @Column(precision = 24, scale = 12)
    private BigDecimal dividendYield;
    @Column(precision = 24, scale = 8)
    private BigDecimal eps;
    @Column(precision = 24, scale = 12)
    private BigDecimal profitMargin;
    @Column(precision = 24, scale = 12)
    private BigDecimal operatingMarginTtm;
    @Column(precision = 24, scale = 12)
    private BigDecimal returnOnAssetsTtm;
    @Column(precision = 24, scale = 12)
    private BigDecimal returnOnEquityTtm;
    @Column(precision = 30, scale = 6)
    private BigDecimal revenueTtm;
    @Column(precision = 30, scale = 6)
    private BigDecimal grossProfitTtm;

    @Column(columnDefinition = "TEXT")
    private String description;
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
