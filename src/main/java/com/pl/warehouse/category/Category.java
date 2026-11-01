package com.pl.warehouse.category;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * STUDY: JPA {@code @Entity} — lookup table for grouping catalog items (Electronics, Tools, …).
 * Unique {@code name}; referenced by {@code WarehouseItem.category}. Do not expose as API —
 * map to {@link com.pl.warehouse.category.dto.CategoryResponse}.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    private String description;
}
