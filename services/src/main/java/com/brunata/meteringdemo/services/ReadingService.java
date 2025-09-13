package com.brunata.meteringdemo.services;

import com.brunata.meteringdemo.common.ValidationException;
import com.brunata.meteringdemo.domain.Device;
import com.brunata.meteringdemo.domain.MeterReading;
import com.brunata.meteringdemo.persistence.DeviceRepository;
import com.brunata.meteringdemo.persistence.MeterReadingRepository;
import com.brunata.meteringdemo.services.dto.ReadingDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service für Messwerte.
 * Regeln:
 * - Zeitliche Monotonie: neuer Messwert darf nicht vor dem letzten liegen
 * - Werte-Monotonie: neuer Zählerstand darf nicht kleiner sein
 */
@Service
public class ReadingService {

    private final DeviceRepository deviceRepo;
    private final MeterReadingRepository readingRepo;

    public ReadingService(DeviceRepository deviceRepo, MeterReadingRepository readingRepo) {
        this.deviceRepo = deviceRepo;
        this.readingRepo = readingRepo;
    }

    /**
     * Speichert einen Messwert nach Validierung gegen die letzte bekannte Messung.
     */
    @Transactional
    public MeterReading save(ReadingDto dto) {
        Device d = deviceRepo.findById(dto.deviceId())
                .orElseThrow(() -> new ValidationException("Unknown device: " + dto.deviceId()));
        var last = readingRepo.findTop1ByDeviceIdOrderByReadingTimeDesc(d.getId());
        last.ifPresent(mr -> {
            if (dto.readingTime().isBefore(mr.getReadingTime())) {
                throw new ValidationException("Reading time must be >= last reading time");
            }
            if (dto.value().compareTo(mr.getValue()) < 0) {
                throw new ValidationException("Monotonicity violated for device " + d.getSerialNo());
            }
        });

        var mr = new MeterReading();
        mr.setDeviceId(dto.deviceId());
        mr.setReadingTime(dto.readingTime());
        mr.setValue(dto.value());
        mr.setUnit(dto.unit());
        mr.setSource(dto.source());

        d.setLastSeenAt(Instant.now());
        deviceRepo.save(d);
        return readingRepo.save(mr);
    }

    /**
     * Liefert Messwerte für ein Gerät (Demo: in-memory Filter; produktiv via Query).
     */
    @Transactional(readOnly = true)
    public List<MeterReading> list(UUID deviceId) {
        return readingRepo.findByDeviceIdOrderByReadingTimeAsc(deviceId);
    }
}
