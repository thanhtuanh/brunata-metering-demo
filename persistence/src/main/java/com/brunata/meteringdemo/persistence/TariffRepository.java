package com.brunata.meteringdemo.persistence;

import com.brunata.meteringdemo.domain.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository für Tariff-CRUD.
 * Spring Data JPA generiert Standardmethoden (findById, save, etc.).
 */
public interface TariffRepository extends JpaRepository<Tariff, UUID> { }
