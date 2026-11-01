package com.pl.warehouse.dashboard;

import com.pl.warehouse.category.CategoryRepository;
import com.pl.warehouse.dashboard.dto.DashboardSummaryResponse;
import com.pl.warehouse.inventory.Inventory;
import com.pl.warehouse.inventory.InventoryRepository;
import com.pl.warehouse.inventory.StockMovementRepository;
import com.pl.warehouse.item.ItemRepository;
import com.pl.warehouse.item.ItemStatus;
import com.pl.warehouse.supplier.SupplierRepository;
import com.pl.warehouse.warehouse.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * STUDY: Read-only aggregations for the CRM dashboard (counts + inventory monetary value).
 *
 * <p>{@code @Transactional(readOnly = true)} is required so LAZY {@code inv.getItem().getPrice()}
 * can load while iterating inventory, and so the whole summary is one consistent read snapshot.
 * No writes here — {@code readOnly} also hints the provider to skip dirty checking / flush.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final WarehouseRepository warehouseRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        long lowStock = 0;
        BigDecimal value = BigDecimal.ZERO;

        for (Inventory inv : inventoryRepository.findAll()) {
            if (inv.getQuantity() < inv.getMinQuantity()) {
                lowStock++;
            }
            BigDecimal line = inv.getItem().getPrice()
                    .multiply(BigDecimal.valueOf(inv.getQuantity()));
            value = value.add(line);
        }

        long activeItems = itemRepository.findAll().stream()
                .filter(i -> i.getStatus() == ItemStatus.ACTIVE)
                .count();

        return new DashboardSummaryResponse(
                warehouseRepository.count(),
                categoryRepository.count(),
                supplierRepository.count(),
                itemRepository.count(),
                activeItems,
                inventoryRepository.count(),
                lowStock,
                value,
                stockMovementRepository.count()
        );
    }
}
