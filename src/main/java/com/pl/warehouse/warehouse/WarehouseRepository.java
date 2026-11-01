package com.pl.warehouse.warehouse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * STUDY: Spring Data JPA repository for warehouses.
 * {@code existsByNameIgnoreCase} — derived query from method name (uniqueness check before insert).
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    boolean existsByNameIgnoreCase(String name);
}
