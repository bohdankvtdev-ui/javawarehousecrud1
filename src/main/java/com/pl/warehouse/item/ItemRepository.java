
package com.pl.warehouse.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<WarehouseItem, UUID> {

    Page<WarehouseItem> findByItemNameContainingIgnoreCase(
            String itemName,
            Pageable pageable
    );

    Page<WarehouseItem> findByCategoryId(
            UUID categoryId,
            Pageable pageable
    );

    Page<WarehouseItem> findByStatus(
            ItemStatus status,
            Pageable pageable
    );
}
