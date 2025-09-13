package com.brunata.meteringdemo.api;

import com.brunata.meteringdemo.domain.MeterReading;
import com.brunata.meteringdemo.services.ReadingService;
import com.brunata.meteringdemo.services.dto.ReadingDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST-Controller für Messwerte (Readings).
 * Verantwortlichkeiten:
 * - Validierte DTOs entgegennehmen und in Domain-Objekte speichern
 * - Liste der Messwerte für ein Gerät liefern
 */
@RestController
@RequestMapping("/api/readings")
public class ReadingController {

    private final ReadingService service;

    public ReadingController(ReadingService service) { this.service = service; }

    /**
     * Nimmt eine Liste validierter Messwerte entgegen und speichert diese.
     */
    @PostMapping
    public List<MeterReading> ingest(@Valid @RequestBody List<@Valid ReadingDto> readings){
        return readings.stream().map(service::save).toList();
    }

    /**
     * Liefert alle Messwerte für ein Gerät (vereinfachte Demo-Implementierung).
     */
    @GetMapping
    public List<MeterReading> list(@RequestParam UUID deviceId){
        return service.list(deviceId);
    }
}
