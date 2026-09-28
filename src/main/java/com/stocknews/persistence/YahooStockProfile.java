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
@Table(name = "yahoo_stock_profiles")
@Getter
@Setter
@NoArgsConstructor
public class YahooStockProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    private String name;
    private String country;
    private String city;
    private String state;
    private String address;
    private String postalCode;
    private String phone;
    private String website;
    private String sector;
    private String industry;
    private Long fullTimeEmployees;
    private String currency;

    @Column(precision = 24, scale = 6)
    private BigDecimal marketCapitalization;

    @Column(columnDefinition = "TEXT")
    private String businessSummary;

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
