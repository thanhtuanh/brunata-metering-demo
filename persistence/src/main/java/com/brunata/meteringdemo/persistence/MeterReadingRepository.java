package com.brunata.meteringdemo.persistence;

import com.brunata.meteringdemo.domain.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository für Messwerte.
 * Enthält eine Beispiel-Query, die den neuesten Messwert eines Geräts liefert.
 */
public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {

    /**
     * Liefert den neuesten Messwert eines Geräts anhand der Zeit (LIMIT 1 via Method-Derivation).
     */
    Optional<MeterReading> findTop1ByDeviceIdOrderByReadingTimeDesc(UUID deviceId);

    /**
     * Erster Messwert im Zeitraum [from, to) nach Zeit aufsteigend.
     */
    Optional<MeterReading> findFirstByDeviceIdAndReadingTimeGreaterThanEqualAndReadingTimeLessThanOrderByReadingTimeAsc(
            UUID deviceId, Instant from, Instant to);

    /**
     * Letzter Messwert im Zeitraum [from, to) nach Zeit absteigend.
     */
    Optional<MeterReading> findFirstByDeviceIdAndReadingTimeGreaterThanEqualAndReadingTimeLessThanOrderByReadingTimeDesc(
            UUID deviceId, Instant from, Instant to);

    /**
     * Aggregierte Verbrauchsberechnung in der Datenbank (PostgreSQL):
     * consumption = max(value) - min(value) innerhalb des Zeitraums.
     */
    @Query(value = """
            select (max(value) - min(value)) as consumption
            from meter_reading
            where device_id = :deviceId
              and reading_time >= :from and reading_time < :to
            """, nativeQuery = true)
    Optional<BigDecimal> computeConsumption(UUID deviceId, Instant from, Instant to);

    /**
     * Alle Messwerte eines Geräts (Demo: für einfache Listenansicht); besser als findAll()+Filter.
     */
    java.util.List<MeterReading> findByDeviceIdOrderByReadingTimeAsc(UUID deviceId);
}
