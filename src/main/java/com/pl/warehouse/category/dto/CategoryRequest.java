package com.pl.warehouse.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * STUDY: Request DTO for create/update category. Validated when the controller uses {@code @Valid}.
 */
public record CategoryRequest(
        @NotBlank @Size(max = 100) String name,
        String description
) {
}
