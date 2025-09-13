package com.brunata.meteringdemo.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Vertrag zwischen Kunde und Messgerät (Device) mit zugeordnetem Tarif.
 * Enthält Gültigkeitszeitraum und Referenz auf Tariff.
 */
@Entity
public class Contract {
    @Id
    private UUID id;

    // Kundenname (vereinfachte Modellierung)
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    // Referenz auf Device (nur UUID statt @ManyToOne, um Kopplung zu reduzieren)
    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    // Vertragsbeginn
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // Vertragsende (optional)
    @Column(name = "end_date")
    private LocalDate endDate;

    // Verknüpfung auf Tarif (FK-Spalte tariff_id)
    @ManyToOne(optional = false)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    @PrePersist public void pre() { if (id == null) id = UUID.randomUUID(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Tariff getTariff() { return tariff; }
    public void setTariff(Tariff tariff) { this.tariff = tariff; }
}
