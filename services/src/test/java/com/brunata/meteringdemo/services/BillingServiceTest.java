package com.brunata.meteringdemo.services;

import com.brunata.meteringdemo.common.ValidationException;
import com.brunata.meteringdemo.domain.Contract;
import com.brunata.meteringdemo.domain.Invoice;
import com.brunata.meteringdemo.domain.MeterReading;
import com.brunata.meteringdemo.domain.Tariff;
import com.brunata.meteringdemo.persistence.ContractRepository;
import com.brunata.meteringdemo.persistence.InvoiceRepository;
import com.brunata.meteringdemo.persistence.MeterReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BillingServiceTest {
    /**
     * Tests für den BillingService:
     * - Betrag aus Verbrauch (DB-Aggregat) und Tarif berechnen (Rundung)
     * - Ungültige Zeiträume -> ValidationException
     * - Keine Messwerte im Zeitraum -> ValidationException
     */

    private ContractRepository contractRepo;
    private MeterReadingRepository readingRepo;
    private InvoiceRepository invoiceRepo;
    private BillingService service;

    @BeforeEach
    void setup() {
        contractRepo = mock(ContractRepository.class);
        readingRepo = mock(MeterReadingRepository.class);
        invoiceRepo = mock(InvoiceRepository.class);
        var props = new com.brunata.meteringdemo.services.config.BillingProperties(2, java.math.RoundingMode.HALF_UP);
        service = new BillingService(contractRepo, readingRepo, invoiceRepo, props);
    }

    @Test
    void calculates_amount_from_min_max_readings() {
        var tariff = new Tariff();
        tariff.setPricePerUnit(new BigDecimal("0.2500"));
        tariff.setUnit("kWh");

        var contractId = UUID.randomUUID();
        var deviceId = UUID.randomUUID();
        var contract = new Contract();
        contract.setId(contractId);
        contract.setDeviceId(deviceId);
        contract.setTariff(tariff);

        when(contractRepo.findById(contractId)).thenReturn(Optional.of(contract));

        var r1 = new MeterReading();
        r1.setDeviceId(deviceId);
        r1.setReadingTime(Instant.parse("2025-09-10T00:00:00Z"));
        r1.setValue(new BigDecimal("100.000000"));
        r1.setUnit("kWh");

        var r2 = new MeterReading();
        r2.setDeviceId(deviceId);
        r2.setReadingTime(Instant.parse("2025-09-30T00:00:00Z"));
        r2.setValue(new BigDecimal("160.500000"));
        r2.setUnit("kWh");

        // DB-seitige Aggregat-Query für Verbrauch
        when(readingRepo.computeConsumption(eq(deviceId), any(), any()))
                .thenReturn(Optional.of(new BigDecimal("60.500000")));

        // echo the invoice back on save
        when(invoiceRepo.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        var from = LocalDate.parse("2025-09-01");
        var to = LocalDate.parse("2025-09-30");
        var invoice = service.run(contractId, from, to);

        // consumption = 60.500000; amount = 60.5 * 0.25 = 15.125 -> 15.13
        assertThat(invoice.getConsumption()).isEqualByComparingTo("60.500000");
        assertThat(invoice.getAmount()).isEqualByComparingTo("15.13");
        assertThat(invoice.getStatus()).isEqualTo("OPEN");

        var captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepo).save(captor.capture());
        assertThat(captor.getValue().getPeriodFrom()).isEqualTo(from);
        assertThat(captor.getValue().getPeriodTo()).isEqualTo(to);
    }

    @Test
    void throws_on_invalid_period() {
        var id = UUID.randomUUID();
        when(contractRepo.findById(id)).thenReturn(Optional.of(new Contract()));
        assertThatThrownBy(() -> service.run(id, LocalDate.parse("2025-10-01"), LocalDate.parse("2025-09-30")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("periodTo < periodFrom");
    }

    @Test
    void throws_when_no_readings() {
        var contract = new Contract();
        contract.setId(UUID.randomUUID());
        contract.setDeviceId(UUID.randomUUID());
        var tariff = new Tariff();
        tariff.setPricePerUnit(new BigDecimal("0.10"));
        tariff.setUnit("kWh");
        contract.setTariff(tariff);

        when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
        when(readingRepo.computeConsumption(any(), any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.run(contract.getId(), LocalDate.parse("2025-09-01"), LocalDate.parse("2025-09-30")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("No readings in period");
    }
}
