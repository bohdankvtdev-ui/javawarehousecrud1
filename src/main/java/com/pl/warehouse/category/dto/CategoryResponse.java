package com.pl.warehouse.category.dto;

import java.util.UUID;

/**
 * STUDY: Response DTO for category — stable API shape; do not return the JPA entity.
 */
public record CategoryResponse(
        UUID id,
        String name,
        String description
) {}
