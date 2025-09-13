package com.brunata.meteringdemo.api;

import com.brunata.meteringdemo.domain.Contract;
import com.brunata.meteringdemo.domain.Device;
import com.brunata.meteringdemo.domain.MeterReading;
import com.brunata.meteringdemo.domain.Tariff;
import com.brunata.meteringdemo.persistence.ContractRepository;
import com.brunata.meteringdemo.persistence.DeviceRepository;
import com.brunata.meteringdemo.persistence.MeterReadingRepository;
import com.brunata.meteringdemo.persistence.TariffRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Seed-Controller für Demo-Daten.
 *
 * Endpunkte:
 * - POST/GET `/api/demo/seed` erzeugt einen Beispiel-Tarif, ein Gerät, einen Vertrag
 *   und zwei Messwerte (früher/später), damit andere Flows (Billing, Listing) getestet werden können.
 *
 * Hinweise:
 * - Werte sind bewusst deterministisch/überschaubar gewählt (kWh, 0.25 EUR/kWh),
 *   damit Berechnungen im Billing-Service nachvollziehbar sind.
 */
@RestController
@Tag(name = "Demo", description = "Demo-Daten erzeugen")
@RequestMapping("/api/demo")
public class DemoSeedController {

    // Repositories als Datenzugriffsschicht (per Spring injiziert)
    private final DeviceRepository deviceRepo;
    private final TariffRepository tariffRepo;
    private final ContractRepository contractRepo;
    private final MeterReadingRepository readingRepo;

    public DemoSeedController(DeviceRepository deviceRepo,
                              TariffRepository tariffRepo,
                              ContractRepository contractRepo,
                              MeterReadingRepository readingRepo) {
        this.deviceRepo = deviceRepo;
        this.tariffRepo = tariffRepo;
        this.contractRepo = contractRepo;
        this.readingRepo = readingRepo;
    }

    @PostMapping("/seed")
    @Transactional
    public Map<String, Object> seed() {
        // 1) Beispiel-Tarif anlegen (0.25 EUR/kWh)
        Tariff t = new Tariff();
        t.setName("Standard");
        t.setPricePerUnit(new BigDecimal("0.2500"));
        t.setUnit("kWh");
        t = tariffRepo.save(t);

        // 2) Demo-Gerät erzeugen (HEAT, zufällige Seriennummer)
        Device d = new Device();
        d.setType("HEAT");
        d.setSerialNo("DEMO-" + UUID.randomUUID().toString().substring(0, 8));
        d.setLocation("Demo");
        d = deviceRepo.save(d);

        // 3) Vertrag mit Startdatum heute und verknüpftem Tarif
        Contract c = new Contract();
        c.setCustomerName("Musterkunde");
        c.setDeviceId(d.getId());
        c.setStartDate(LocalDate.now());
        c.setTariff(t);
        c = contractRepo.save(c);

        // 4) Zwei Messzeitpunkte im aktuellen Monat (10. und 25. Tag, 00:00 UTC)
        Instant r1t = LocalDate.now().withDayOfMonth(10).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant r2t = LocalDate.now().withDayOfMonth(25).atStartOfDay().toInstant(ZoneOffset.UTC);

        // 5) Erster Messwert: 100.000000 kWh
        MeterReading r1 = new MeterReading();
        r1.setDeviceId(d.getId());
        r1.setReadingTime(r1t);
        r1.setValue(new BigDecimal("100.000000"));
        r1.setUnit("kWh");
        r1.setSource("Demo");
        r1 = readingRepo.save(r1);

        // 6) Zweiter Messwert: 160.500000 kWh (ergibt Verbrauch 60.5 kWh)
        MeterReading r2 = new MeterReading();
        r2.setDeviceId(d.getId());
        r2.setReadingTime(r2t);
        r2.setValue(new BigDecimal("160.500000"));
        r2.setUnit("kWh");
        r2.setSource("Demo");
        r2 = readingRepo.save(r2);

        // 7) IDs zur einfachen Weiterverwendung zurückgeben (z. B. für API-Calls im Tutorial)
        Map<String, Object> res = new HashMap<>();
        res.put("deviceId", d.getId());
        res.put("contractId", c.getId());
        res.put("tariffId", t.getId());
        res.put("readingIds", new UUID[]{r1.getId(), r2.getId()});
        return res;
    }

    /**
     * Fallback: erlaubt GET /api/demo/seed (praktisch für Browser-Aufruf ohne Tooling).
     */
    @GetMapping("/seed")
    @Transactional
    public Map<String, Object> seedGet() {
        return seed();
    }
}
