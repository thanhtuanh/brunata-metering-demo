package com.brunata.meteringdemo.persistence;

import com.brunata.meteringdemo.domain.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository für Contract-CRUD.
 */
public interface ContractRepository extends JpaRepository<Contract, UUID> { }
