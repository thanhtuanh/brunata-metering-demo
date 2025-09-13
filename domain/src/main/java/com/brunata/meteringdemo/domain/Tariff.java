package com.brunata.meteringdemo.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Tarif mit Preis je Einheit (z. B. kWh, m³).
 * Persistiert über JPA und von Verträgen referenziert.
 */
@Entity
public class Tariff {
    @Id
    private UUID id;

    // Anzeigename des Tarifs, z. B. "Standard"
    @Column(nullable = false)
    private String name;

    // Preis pro Einheit, feste Genauigkeit für Geld/Preis-Felder
    @Column(name = "price_per_unit", nullable = false, precision = 12, scale = 4)
    private BigDecimal pricePerUnit;

    // Einheit, auf die sich der Preis bezieht (z. B. "kWh")
    @Column(nullable = false)
    private String unit;

    // Generiert eine UUID, falls keine gesetzt ist (beim Insert)
    @PrePersist public void pre() { if (id == null) id = UUID.randomUUID(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
