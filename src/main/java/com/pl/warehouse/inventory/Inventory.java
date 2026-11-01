package com.pl.warehouse.inventory;


import com.pl.warehouse.item.WarehouseItem;
import com.pl.warehouse.warehouse.Warehouse;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name="inventory",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_inventory_warehouse_item",
                columnNames = {"warehouse_id","item_id"}
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
    @JoinColumn(name = "warehouse_id",nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private WarehouseItem item;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "min_quantity", nullable = false)
    private Integer minQuantity = 0;

    private String location;

    @Column(name = "warehouse_section")
    private String warehouseSection;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    protected void updateTimestamp(){
        updatedAt = Instant.now();
    }


}
