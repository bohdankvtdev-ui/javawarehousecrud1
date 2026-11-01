package com.pl.warehouse.warehouse;

import com.pl.warehouse.warehouse.dto.WarehouseRequest;
import com.pl.warehouse.warehouse.dto.WarehouseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * STUDY — Thin HTTP adapter for physical warehouse locations.
 * Inventory rows reference these IDs ({@code warehouse_id}). {@code @Valid} +
 * CREATED / NO_CONTENT status codes. Frontend route: {@code /warehouses}.
 *
 * <p>Seed IDs (see V6):
 * Central  = a0000000-0000-4000-8000-000000000001
 * Northern = a0000000-0000-4000-8000-000000000002
 */
@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public List<WarehouseResponse> getAll() {
        return warehouseService.getAll();
    }

    @GetMapping("/{id}")
    public WarehouseResponse getById(@PathVariable UUID id) {
        return warehouseService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseResponse create(@Valid @RequestBody WarehouseRequest request) {
        return warehouseService.create(request);
    }

    @PutMapping("/{id}")
    public WarehouseResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody WarehouseRequest request
    ) {
        return warehouseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        warehouseService.delete(id);
    }
}
