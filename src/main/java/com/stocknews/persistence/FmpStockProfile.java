package com.stocknews.persistence;

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
@Table(name = "fmp_stock_profiles")
@Getter
@Setter
@NoArgsConstructor
public class FmpStockProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    private String companyName;
    private String currency;
    private String cik;
    private String isin;
    private String cusip;
    private String exchange;
    private String exchangeShortName;
    private String industry;
    private String website;

    @Column(precision = 30, scale = 6)
    private BigDecimal marketCap;

    @Column(precision = 24, scale = 8)
    private BigDecimal price;

    @Column(precision = 24, scale = 8)
    private BigDecimal beta;

    @Column(precision = 24, scale = 8)
    private BigDecimal lastDividend;

    private String priceRange;

    @Column(precision = 24, scale = 8)
    private BigDecimal change;

    @Column(precision = 24, scale = 8)
    private BigDecimal changePercentage;

    private Long volume;
    private Long averageVolume;
    private String ceo;
    private String sector;
    private String country;
    private Long fullTimeEmployees;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String ipoDate;
    private Boolean activelyTrading;
    private Boolean etf;
    private Boolean fund;
    private Boolean adr;
    private Boolean delisted;

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
