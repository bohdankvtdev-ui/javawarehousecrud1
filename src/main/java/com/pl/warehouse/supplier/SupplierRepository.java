package com.pl.warehouse.supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * STUDY: Spring Data JPA repository — CRUD only; method naming kicks in when you add finders.
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
}
