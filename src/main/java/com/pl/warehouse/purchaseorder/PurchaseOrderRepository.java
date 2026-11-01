package com.pl.warehouse.purchaseorder;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * STUDY: {@code @EntityGraph} loads lines + names in one query (avoids N+1 on detail view).
 */
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    boolean existsByPoNumberIgnoreCase(String poNumber);

    @EntityGraph(attributePaths = {"supplier", "warehouse", "lines", "lines.item"})
    Optional<PurchaseOrder> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"supplier", "warehouse"})
    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();
}
