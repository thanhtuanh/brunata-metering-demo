package com.brunata.meteringdemo.persistence;

import com.brunata.meteringdemo.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository für Geräte (Device).
 */
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    /**
     * Findet ein Gerät anhand der Seriennummer.
     */
    Optional<Device> findBySerialNo(String serialNo);

    /**
     * Liefert Geräte, die noch nie gesehen wurden oder seit dem Cutoff-Zeitpunkt offline sind.
     * Ermöglicht effiziente Auswahl für Integrationsjobs (mit Index auf last_seen_at).
     */
    List<Device> findByLastSeenAtIsNullOrLastSeenAtBefore(Instant cutoff);
}
