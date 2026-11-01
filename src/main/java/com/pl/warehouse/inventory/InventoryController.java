package com.pl.warehouse.inventory;

import com.pl.warehouse.inventory.dto.InventoryRequest;
import com.pl.warehouse.inventory.dto.InventoryResponse;
import com.pl.warehouse.inventory.dto.InventoryStockReport;
import com.pl.warehouse.inventory.dto.ReorderSuggestionResponse;
import com.pl.warehouse.inventory.dto.StockAdjustRequest;
import com.pl.warehouse.inventory.dto.StockMovementResponse;
import com.pl.warehouse.inventory.dto.StockTransferRequest;
import com.pl.warehouse.inventory.jdbc.InventoryReportJdbcRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * STUDY — Thin HTTP adapter for warehouse CRM inventory.
 *
 * <p>Delegates to {@link InventoryService} (JPA path) and
 * {@link InventoryReportJdbcRepository} (SQL report). {@code @Valid} validates
 * request DTOs; {@code @ResponseStatus} sets CREATED / NO_CONTENT.
 * Keep controllers free of transaction / quantity rules.
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryReportJdbcRepository inventoryReportJdbcRepository;

    @GetMapping
    public List<InventoryResponse> getAll() {
        return inventoryService.getAll();
    }

    /** Purchasing queue: qty &lt; min across warehouses. */
    @GetMapping("/reorder-suggestions")
    public List<ReorderSuggestionResponse> reorderSuggestions() {
        return inventoryService.reorderSuggestions();
    }

    @GetMapping("/movements")
    public List<StockMovementResponse> recentMovements(
            @RequestParam(defaultValue = "20") int limit
    ) {
        return inventoryService.recentMovements(limit);
    }

    /** Literal paths before /{id} so "by-warehouse" is never parsed as a UUID. */
    @GetMapping("/by-warehouse/{warehouseId}")
    public List<InventoryResponse> getByWarehouse(@PathVariable UUID warehouseId) {
        return inventoryService.getByWarehouse(warehouseId);
    }

    @GetMapping("/low-stock/{warehouseId}")
    public List<InventoryResponse> getLowStock(@PathVariable UUID warehouseId) {
        return inventoryService.getLowStock(warehouseId);
    }

    /** JDBC report path — study contrast with JPA list endpoints above. */
    @GetMapping("/reports/stock")
    public List<InventoryStockReport> stockReport() {
        return inventoryReportJdbcRepository.findStockReport();
    }

    @GetMapping("/{id}")
    public InventoryResponse getById(@PathVariable UUID id) {
        return inventoryService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse create(@Valid @RequestBody InventoryRequest request) {
        return inventoryService.create(request);
    }

    @PutMapping("/stock")
    public InventoryResponse upsert(@Valid @RequestBody InventoryRequest request) {
        return inventoryService.upsert(request);
    }

    /** Receive (+) or ship (-) stock on an existing inventory row. */
    @PostMapping("/{id}/adjust")
    public InventoryResponse adjust(
            @PathVariable UUID id,
            @Valid @RequestBody StockAdjustRequest request
    ) {
        return inventoryService.adjust(id, request);
    }

    /** Move stock between warehouses for one item (all-or-nothing in the service). */
    @PostMapping("/transfer")
    public List<InventoryResponse> transfer(@Valid @RequestBody StockTransferRequest request) {
        return inventoryService.transfer(request);
    }

    @PutMapping("/{id}")
    public InventoryResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody InventoryRequest request
    ) {
        return inventoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        inventoryService.delete(id);
    }
}
