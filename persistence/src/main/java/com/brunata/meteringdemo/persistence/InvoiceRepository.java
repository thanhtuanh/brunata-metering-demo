package com.brunata.meteringdemo.persistence;

import com.brunata.meteringdemo.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;
import java.time.LocalDate;

/**
 * Repository für Invoice-CRUD.
 */
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    /**
     * Liefert eine Rechnung für einen Vertrag und einen exakten Zeitraum, falls vorhanden.
     * Ermöglicht Idempotenz von Billing-Requests.
     */
    Optional<Invoice> findByContractIdAndPeriodFromAndPeriodTo(UUID contractId, LocalDate from, LocalDate to);
}
