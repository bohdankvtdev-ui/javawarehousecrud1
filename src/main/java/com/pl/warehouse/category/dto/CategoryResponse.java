package com.pl.warehouse.category.dto;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String description
) {}
