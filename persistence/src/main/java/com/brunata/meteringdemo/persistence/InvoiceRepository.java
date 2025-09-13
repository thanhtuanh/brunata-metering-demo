package com.brunata.meteringdemo.persistence;

import com.brunata.meteringdemo.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository für Invoice-CRUD.
 */
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> { }
