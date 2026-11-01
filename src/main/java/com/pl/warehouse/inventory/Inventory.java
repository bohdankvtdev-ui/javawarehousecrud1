package com.pl.warehouse.inventory;

import com.pl.warehouse.item.WarehouseItem;
import com.pl.warehouse.warehouse.Warehouse;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * STUDY: Stock on hand — one row per (warehouse, item).
 *
 * <p>{@code @Version} = optimistic locking. Hibernate sends {@code WHERE version=?} on UPDATE;
 * if another transaction already changed the row → {@code OptimisticLockException} → HTTP 409.
 *
 * <p>{@code reservedQuantity} = held for orders; {@link #available()} = quantity − reserved.
 */
@Entity
@Table(
        name = "inventory",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_inventory_warehouse_item",
                columnNames = {"warehouse_id", "item_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private WarehouseItem item;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "min_quantity", nullable = false)
    private Integer minQuantity = 0;

    /**
     * STUDY: optimistic lock. Starts at {@code 0} on INSERT; increments on each UPDATE.
     * Seeing 0 on every row means those rows were never updated since seed — normal.
     * Adjust / transfer / receive → version becomes 1, 2, … (conflict → HTTP 409).
     */
    @Version
    private Long version;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    private String location;

    @Column(name = "warehouse_section")
    private String warehouseSection;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public int available() {
        return quantity - reservedQuantity;
    }

    @PrePersist
    @PreUpdate
    protected void updateTimestamp() {
        updatedAt = Instant.now();
    }
}
