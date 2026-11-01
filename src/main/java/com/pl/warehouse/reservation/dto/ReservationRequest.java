package com.pl.warehouse.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ReservationRequest(
        @NotNull UUID warehouseId,
        @NotNull UUID itemId,
        @NotNull @Min(1) Integer quantity,
        @NotBlank @Size(max = 80) String reference,
        @Size(max = 255) String notes
) {
}
