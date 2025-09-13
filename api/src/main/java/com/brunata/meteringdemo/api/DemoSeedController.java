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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/demo")
public class DemoSeedController {

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
        Tariff t = new Tariff();
        t.setName("Standard");
        t.setPricePerUnit(new BigDecimal("0.2500"));
        t.setUnit("kWh");
        t = tariffRepo.save(t);

        Device d = new Device();
        d.setType("HEAT");
        d.setSerialNo("DEMO-" + UUID.randomUUID().toString().substring(0, 8));
        d.setLocation("Demo");
        d = deviceRepo.save(d);

        Contract c = new Contract();
        c.setCustomerName("Musterkunde");
        c.setDeviceId(d.getId());
        c.setStartDate(LocalDate.now());
        c.setTariff(t);
        c = contractRepo.save(c);

        Instant r1t = LocalDate.now().withDayOfMonth(10).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant r2t = LocalDate.now().withDayOfMonth(25).atStartOfDay().toInstant(ZoneOffset.UTC);

        MeterReading r1 = new MeterReading();
        r1.setDeviceId(d.getId());
        r1.setReadingTime(r1t);
        r1.setValue(new BigDecimal("100.000000"));
        r1.setUnit("kWh");
        r1.setSource("Demo");
        r1 = readingRepo.save(r1);

        MeterReading r2 = new MeterReading();
        r2.setDeviceId(d.getId());
        r2.setReadingTime(r2t);
        r2.setValue(new BigDecimal("160.500000"));
        r2.setUnit("kWh");
        r2.setSource("Demo");
        r2 = readingRepo.save(r2);

        Map<String, Object> res = new HashMap<>();
        res.put("deviceId", d.getId());
        res.put("contractId", c.getId());
        res.put("tariffId", t.getId());
        res.put("readingIds", new UUID[]{r1.getId(), r2.getId()});
        return res;
    }

    // Fallback: erlaubt auch GET /api/demo/seed (z. B. für einfache Browser-Tests)
    @GetMapping("/seed")
    @Transactional
    public Map<String, Object> seedGet() {
        return seed();
    }
}
