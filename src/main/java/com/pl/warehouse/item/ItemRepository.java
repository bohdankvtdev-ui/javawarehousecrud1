package com.pl.warehouse.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * STUDY: Spring Data JPA repository for catalog items.
 *
 * <p>{@link JpaRepository} → CRUD. {@link JpaSpecificationExecutor} → dynamic {@link ItemSpecs}.
 *
 * <h2>N+1 fix ({@code @EntityGraph})</h2>
 * Mapping DTOs needs category + supplier names.
 * <b>Before:</b> 1 query for page + N category + N supplier selects.<br>
 * <b>After:</b> one fetch graph loads associations with the page (see QUERY_OPTIMIZATION.md).
 */
@Repository
public interface ItemRepository extends JpaRepository<WarehouseItem, UUID>,
        JpaSpecificationExecutor<WarehouseItem> {

    @Override
    @EntityGraph(attributePaths = {"category", "supplier"})
    Page<WarehouseItem> findAll(@Nullable Specification<WarehouseItem> spec, Pageable pageable);
}
