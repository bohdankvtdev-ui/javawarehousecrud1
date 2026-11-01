package com.pl.warehouse.item;

import com.pl.warehouse.category.Category;
import com.pl.warehouse.supplier.Supplier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * STUDY: JPA {@code @Entity} = Java class mapped to a database table.
 *
 * <p>WarehouseItem = catalog SKU (what you sell/stock), NOT the stock quantity.
 * Stock lives in {@code Inventory} (item + warehouse + quantity).
 *
 * <p>Do not return this entity from controllers — map to {@link com.pl.warehouse.item.dto.ItemResponse}
 * so the API stays stable and does not leak persistence details / LAZY proxies.
 *
 * <p>Annotations:
 * <ul>
 *   <li>{@code @Entity} / {@code @Table} — persistence mapping</li>
 *   <li>{@code @ManyToOne(LAZY)} — FK to category/supplier; load only when accessed (needs an open transaction)</li>
 *   <li>{@code @Enumerated(STRING)} — store enum name, not ordinal (safer for schema evolution)</li>
 * </ul>
 */
@Entity
@Table(name = "warehouse_items")
@Getter
@Setter
@NoArgsConstructor
public class WarehouseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 50)
    private String type;

    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ItemStatus status = ItemStatus.ACTIVE;
}
