package com.pl.warehouse.warehouse;

import com.pl.warehouse.common.exception.ResourceNotFoundException;
import com.pl.warehouse.warehouse.dto.WarehouseRequest;
import com.pl.warehouse.warehouse.dto.WarehouseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * STUDY: Business logic for physical warehouse locations. Inventory rows reference these.
 *
 * <p>{@code @Transactional} = DB boundary; {@code readOnly = true} on getters.
 * In a future inventory-service split, warehouse IDs may become external references
 * (no shared FK across service databases).
 */
@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    /** {@code readOnly}: location list for inventory filters / forms. */
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAll() {
        return warehouseRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WarehouseResponse getById(UUID id) {
        return toResponse(find(id));
    }

    /** {@code @Transactional}: uniqueness check + insert as one unit of work. */
    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        if (warehouseRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException("Warehouse already exists: " + request.name());
        }
        Warehouse warehouse = new Warehouse();
        apply(warehouse, request);
        return toResponse(warehouseRepository.save(warehouse));
    }

    @Transactional
    public WarehouseResponse update(UUID id, WarehouseRequest request) {
        Warehouse warehouse = find(id);
        apply(warehouse, request);
        return toResponse(warehouseRepository.save(warehouse));
    }

    @Transactional
    public void delete(UUID id) {
        warehouseRepository.delete(find(id));
    }

    private Warehouse find(UUID id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));
    }

    private void apply(Warehouse warehouse, WarehouseRequest request) {
        warehouse.setName(request.name());
        warehouse.setAddress(request.address());
        warehouse.setCity(request.city());
        warehouse.setCapacity(request.capacity());
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getAddress(),
                warehouse.getCity(),
                warehouse.getCapacity(),
                warehouse.getCreatedAt()
        );
    }
}
