package com.pl.warehouse.supplier;

import com.pl.warehouse.common.exception.ResourceNotFoundException;
import com.pl.warehouse.supplier.dto.SupplierRequest;
import com.pl.warehouse.supplier.dto.SupplierResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * STUDY: Business logic for suppliers linked from catalog items.
 * {@code @Transactional} = DB boundary for writes; {@code readOnly = true} for list/get.
 */
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    /** {@code readOnly}: dropdown / table reads with no dirty checking. */
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAll() {
        return supplierRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse getById(UUID id) {
        return toResponse(find(id));
    }

    /** {@code @Transactional}: apply fields + save as one commit. */
    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Supplier supplier = new Supplier();
        apply(supplier, request);
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse update(UUID id, SupplierRequest request) {
        Supplier supplier = find(id);
        apply(supplier, request);
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void delete(UUID id) {
        supplierRepository.delete(find(id));
    }

    private Supplier find(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }

    private void apply(Supplier supplier, SupplierRequest request) {
        supplier.setName(request.name());
        supplier.setEmail(request.email());
        supplier.setPhone(request.phone());
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getEmail(),
                supplier.getPhone()
        );
    }
}
