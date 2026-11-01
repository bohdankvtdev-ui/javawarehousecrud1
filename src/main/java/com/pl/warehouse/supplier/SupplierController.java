package com.pl.warehouse.supplier;

import com.pl.warehouse.supplier.dto.SupplierRequest;
import com.pl.warehouse.supplier.dto.SupplierResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * STUDY — Thin HTTP adapter for supplier CRUD.
 * Suppliers are linked from {@code WarehouseItem.supplier}. {@code @Valid} on bodies;
 * CREATED / NO_CONTENT via {@code @ResponseStatus}. Frontend route: {@code /suppliers}.
 */
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public List<SupplierResponse> getAll() {
        return supplierService.getAll();
    }

    @GetMapping("/{id}")
    public SupplierResponse getById(@PathVariable UUID id) {
        return supplierService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponse create(@Valid @RequestBody SupplierRequest request) {
        return supplierService.create(request);
    }

    @PutMapping("/{id}")
    public SupplierResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody SupplierRequest request
    ) {
        return supplierService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        supplierService.delete(id);
    }
}
