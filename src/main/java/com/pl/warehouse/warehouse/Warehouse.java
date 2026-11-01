package com.pl.warehouse.warehouse;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * STUDY: JPA {@code @Entity} for a physical warehouse location.
 * Inventory rows reference this via {@code warehouse_id}. {@code @PrePersist} sets
 * {@code createdAt} before the first INSERT. Do not expose as API — use
 * {@link com.pl.warehouse.warehouse.dto.WarehouseResponse}.
 */
@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    private String address;
    private String city;
    private Integer capacity;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
