package com.brunata.meteringdemo.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Rechnung mit Zeitraum, Verbrauch und Betrag.
 * Wird aus Contract und Messwerten berechnet und persistiert.
 */
@Entity
public class Invoice {
    @Id
    private UUID id;

    // Zugehöriger Vertrag (FK: contract_id)
    @ManyToOne(optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    // Abrechnungszeitraum von/bis (inklusive)
    @Column(name = "period_from", nullable = false)
    private LocalDate periodFrom;

    @Column(name = "period_to", nullable = false)
    private LocalDate periodTo;

    // Ermittelter Verbrauch im Zeitraum (z. B. kWh)
    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal consumption;

    // Betrag in Währungseinheiten, auf 2 Nachkommastellen gerundet
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    // Status der Rechnung (einfaches String-Feld für Demo)
    @Column(nullable = false)
    private String status = "OPEN";

    // Erstellzeitpunkt (Serverzeit)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    // ID-Generierung beim Persistieren
    @PrePersist public void pre() { if (id == null) id = UUID.randomUUID(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Contract getContract() { return contract; }
    public void setContract(Contract contract) { this.contract = contract; }
    public LocalDate getPeriodFrom() { return periodFrom; }
    public void setPeriodFrom(LocalDate periodFrom) { this.periodFrom = periodFrom; }
    public LocalDate getPeriodTo() { return periodTo; }
    public void setPeriodTo(LocalDate periodTo) { this.periodTo = periodTo; }
    public BigDecimal getConsumption() { return consumption; }
    public void setConsumption(BigDecimal consumption) { this.consumption = consumption; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
