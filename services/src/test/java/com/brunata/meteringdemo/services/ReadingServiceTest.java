package com.brunata.meteringdemo.services;

import com.brunata.meteringdemo.common.ValidationException;
import com.brunata.meteringdemo.domain.Device;
import com.brunata.meteringdemo.domain.MeterReading;
import com.brunata.meteringdemo.persistence.DeviceRepository;
import com.brunata.meteringdemo.persistence.MeterReadingRepository;
import com.brunata.meteringdemo.services.dto.ReadingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReadingServiceTest {

    private DeviceRepository deviceRepo;
    private MeterReadingRepository readingRepo;
    private ReadingService service;

    private final UUID deviceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        deviceRepo = mock(DeviceRepository.class);
        readingRepo = mock(MeterReadingRepository.class);
        service = new ReadingService(deviceRepo, readingRepo);

        var dev = new Device();
        dev.setId(deviceId);
        dev.setSerialNo("ABC-123");
        when(deviceRepo.findById(deviceId)).thenReturn(Optional.of(dev));
    }

    @Test
    void rejects_reading_if_time_before_last() {
        var last = new MeterReading();
        last.setDeviceId(deviceId);
        last.setReadingTime(Instant.parse("2025-09-12T10:00:00Z"));
        last.setValue(new BigDecimal("100.0"));
        when(readingRepo.findTop1ByDeviceIdOrderByReadingTimeDesc(deviceId))
                .thenReturn(Optional.of(last));

        var dto = new ReadingDto(
                deviceId,
                Instant.parse("2025-09-12T09:59:59Z"),
                new BigDecimal("101.0"),
                "kWh",
                "LoRa"
        );

        assertThatThrownBy(() -> service.save(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Reading time must be >= last reading time");
        verify(readingRepo, never()).save(any());
    }

    @Test
    void rejects_reading_if_value_decreases() {
        var last = new MeterReading();
        last.setDeviceId(deviceId);
        last.setReadingTime(Instant.parse("2025-09-12T10:00:00Z"));
        last.setValue(new BigDecimal("100.0"));
        when(readingRepo.findTop1ByDeviceIdOrderByReadingTimeDesc(deviceId))
                .thenReturn(Optional.of(last));

        var dto = new ReadingDto(
                deviceId,
                Instant.parse("2025-09-12T10:00:01Z"),
                new BigDecimal("99.9"),
                "kWh",
                "LoRa"
        );

        assertThatThrownBy(() -> service.save(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Monotonicity");
        verify(readingRepo, never()).save(any());
    }
}
