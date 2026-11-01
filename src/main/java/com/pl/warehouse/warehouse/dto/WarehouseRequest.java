package com.pl.warehouse.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * STUDY: Request DTO for create/update warehouse. Bean Validation runs with controller {@code @Valid}.
 */
public record WarehouseRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 255) String address,
        @Size(max = 100) String city,
        @Min(0) Integer capacity
) {
}
