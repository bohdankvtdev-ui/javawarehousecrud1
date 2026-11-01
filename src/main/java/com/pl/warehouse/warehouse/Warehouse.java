package com.pl.warehouse.warehouse;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="warehouses")
@Getter
@Setter
@NoArgsConstructor
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column (nullable=false, unique = true, length = 150)
    private String name;

    private String address;
    private String city;
    private Integer capacity;

    @Column(name = "created_at",nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void OnCreate(){
        createdAt = Instant.now();
    }
}
