package com.brunata.meteringdemo.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Zählerstandsmessung eines Geräts mit Zeitstempel.
 *
 * Modellierungsentscheidungen:
 * - `value` als BigDecimal(18,6) für genaue Verbrauchsdifferenzen (kWh etc.)
 * - `readingTime` in UTC (`Instant`) für eindeutige Zeitvergleiche
 * - `unit`/`source` zur Nachvollziehbarkeit der Datenherkunft
 */
@Entity
public class MeterReading {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID deviceId;
    @Column(nullable = false)
    private Instant readingTime;
    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal value;
    @Column(nullable = false)
    private String unit;
    @Column(nullable = false)
    private String source;

    @PrePersist public void pre() { if (id == null) id = UUID.randomUUID(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public Instant getReadingTime() { return readingTime; }
    public void setReadingTime(Instant readingTime) { this.readingTime = readingTime; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
