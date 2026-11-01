package com.pl.warehouse.supplier;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * STUDY: JPA {@code @Entity} for a vendor. Referenced by {@code WarehouseItem.supplier}.
 * Unique email when set. Map to {@link com.pl.warehouse.supplier.dto.SupplierResponse} for the API —
 * do not expose the entity (or LAZY proxies) over HTTP.
 */
@Entity
@Table(name="suppliers")
@Getter
@Setter
@NoArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 150, unique = true)
    private String email;

    @Column(length = 30)
    private String phone;

}
