package com.brunata.meteringdemo.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Messgerät (z. B. Wärmemengenzähler, Stromzähler).
 *
 * Eigenschaften:
 * - Eindeutige Seriennummer (Unique-Constraint)
 * - Letzter Kontaktzeitpunkt `lastSeenAt` zur Erkennung von Offline-Geräten
 * - Einfache Statuskennzeichnung (Demo)
 */
@Entity
public class Device {
    @Id
    private UUID id;
    // Gerätetyp (frei vergebener String, z. B. "HEAT")
    private String type;
    @Column(unique = true, nullable = false)
    private String serialNo;
    private String location;
    private Instant lastSeenAt;
    private String status = "ACTIVE";

    @PrePersist public void pre() { if (id == null) id = UUID.randomUUID(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSerialNo() { return serialNo; }
    public void setSerialNo(String serialNo) { this.serialNo = serialNo; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Instant getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Instant lastSeenAt) { this.lastSeenAt = lastSeenAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
