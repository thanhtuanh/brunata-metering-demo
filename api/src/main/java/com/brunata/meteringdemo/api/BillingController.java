package com.brunata.meteringdemo.api;

import com.brunata.meteringdemo.domain.Invoice;
import com.brunata.meteringdemo.services.BillingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST-Controller für die Abrechnung (Billing).
 *
 * Verantwortlichkeiten:
 * - Entgegennahme der Request-Parameter (contractId, from, to)
 * - Delegation der Business-Logik an BillingService
 * - Rückgabe der erzeugten Rechnung (Invoice) als JSON
 */
@RestController
@Tag(name = "Billing", description = "Abrechnung erstellen")
@RequestMapping("/api/billing")
public class BillingController {

    // Dependency-Injection des Domänen-Services für Abrechnung
    private final BillingService billing;

    public BillingController(BillingService billing) { this.billing = billing; }

    /**
     * Startet die Abrechnung für einen Vertrag und einen Zeitraum.
     * Beispiel: POST /api/billing/run?contractId=...&from=2025-09-01&to=2025-09-30
     *
     * @param contractId ID des Vertrags (UUID)
     * @param from       Startdatum (inklusive), ISO-Format yyyy-MM-dd
     * @param to         Enddatum (inklusive), ISO-Format yyyy-MM-dd
     * @return erzeugte Rechnung mit Verbrauch und Betrag
     */
    @PostMapping("/run")
    public Invoice run(@RequestParam UUID contractId,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        // Controller bleibt dünn: Validierungen/Logik liegen im Service
        return billing.run(contractId, from, to);
    }
}
