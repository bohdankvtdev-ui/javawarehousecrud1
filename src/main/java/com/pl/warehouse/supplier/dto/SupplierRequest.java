package com.pl.warehouse.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * STUDY: Request DTO for create/update supplier. {@code @Email}/{@code @NotBlank} run with {@code @Valid}.
 */
public record SupplierRequest(
        @NotBlank @Size(max = 150) String name,
        @Email @Size(max = 150) String email,
        @Size(max = 30) String phone
) {
}
