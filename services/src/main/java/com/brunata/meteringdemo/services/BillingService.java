package com.brunata.meteringdemo.services;

import com.brunata.meteringdemo.common.ValidationException;
import com.brunata.meteringdemo.domain.Contract;
import com.brunata.meteringdemo.domain.Invoice;
import com.brunata.meteringdemo.persistence.ContractRepository;
import com.brunata.meteringdemo.persistence.InvoiceRepository;
import com.brunata.meteringdemo.persistence.MeterReadingRepository;
import com.brunata.meteringdemo.services.config.BillingProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Service für die Abrechnung (Billing).
 *
 * Verantwortlichkeiten:
 * - Konsumermittlung aus Messwerten im Zeitraum (min/max)
 * - Preisermittlung aus dem verknüpften Tarif
 * - Erstellen und Persistieren der Rechnung als Transaktion
 */
@Service
public class BillingService {

    // Repositories für Vertragsdaten, Messwerte und Rechnungen
    private final ContractRepository contractRepo;
    private final MeterReadingRepository readingRepo;
    private final InvoiceRepository invoiceRepo;
    private final BillingProperties billingProps;

    public BillingService(ContractRepository contractRepo,
                          MeterReadingRepository readingRepo,
                          InvoiceRepository invoiceRepo,
                          BillingProperties billingProps) {
        this.contractRepo = contractRepo;
        this.readingRepo = readingRepo;
        this.invoiceRepo = invoiceRepo;
        this.billingProps = billingProps;
    }

    /**
     * Führt die Abrechnung für einen Vertrag im Zeitraum [from..to] durch.
     * Transaktional, damit Insert der Rechnung nur bei erfolgreicher Berechnung erfolgt.
     */
    @Transactional
    public Invoice run(UUID contractId, LocalDate from, LocalDate to) {
        // 1) Vertrag laden oder 404/Validation-Fehler
        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> new ValidationException("Unknown contract: " + contractId));

        // 2) Grundvalidierung Zeitraum
        if (to.isBefore(from)) throw new ValidationException("periodTo < periodFrom");

        // 2a) Idempotenz: Existierende Rechnung für Zeitraum zurückgeben
        var existing = invoiceRepo.findByContractIdAndPeriodFromAndPeriodTo(contractId, from, to);
        if (existing.isPresent()) {
            return existing.get();
        }

        // 3) Zeitraum in UTC-Instants (inklusive from, inklusive to bis 23:59:59)
        var fromInstant = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        var toInstant = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        // 4) Verbrauchsberechnung bevorzugt per Aggregat-Query (DB-seitig)
        var consumptionOpt = readingRepo.computeConsumption(contract.getDeviceId(), fromInstant, toInstant);

        var consumption = consumptionOpt.orElseThrow(() ->
                new ValidationException("No readings in period for device " + contract.getDeviceId()));
        if (consumption.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Negative consumption (check meter monotony)!");
        }

        // 6) Betrag = Verbrauch * Preis (auf 2 Nachkommastellen runden)
        var price = contract.getTariff().getPricePerUnit();
        var amount = consumption.multiply(price).setScale(billingProps.scale(), billingProps.roundingMode());

        // 7) Rechnung erzeugen und speichern
        var invoice = new Invoice();
        invoice.setContract(contract);
        invoice.setPeriodFrom(from);
        invoice.setPeriodTo(to);
        invoice.setConsumption(consumption);
        invoice.setAmount(amount);
        invoice.setStatus("OPEN");

        try {
            return invoiceRepo.save(invoice);
        } catch (DataIntegrityViolationException ex) {
            // Race-Condition: paralleler Request hat Rechnung bereits gespeichert -> idempotent zurückgeben
            return invoiceRepo.findByContractIdAndPeriodFromAndPeriodTo(contractId, from, to)
                    .orElseThrow(() -> ex);
        }
    }
}
