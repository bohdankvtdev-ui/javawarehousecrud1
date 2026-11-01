package com.pl.warehouse.item;

import com.pl.warehouse.category.Category;
import com.pl.warehouse.category.CategoryRepository;
import com.pl.warehouse.item.dto.ItemRequest;
import com.pl.warehouse.supplier.Supplier;
import com.pl.warehouse.supplier.SupplierRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public ItemService(
            ItemRepository itemRepository,
            CategoryRepository categoryRepository,
            SupplierRepository supplierRepository
    ) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    // GET ALL ITEMS
    public List<WarehouseItem> getAllItems() {
        return itemRepository.findAll();
    }

    // GET ITEM BY ID
    public WarehouseItem getItemById(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Item not found: " + id)
                );
    }

    // CREATE ITEM
    @Transactional
    public WarehouseItem createItem(ItemRequest request) {

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() ->
                        new RuntimeException("Category not found")
                );

        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() ->
                        new RuntimeException("Supplier not found")
                );

        WarehouseItem item = new WarehouseItem();

        item.setItemName(request.itemName());
        item.setType(request.type());
        item.setDescription(request.description());
        item.setCategory(category);
        item.setSupplier(supplier);
        item.setPrice(request.price());
        item.setWeight(request.weight());

        return itemRepository.save(item);
    }

    // DELETE ITEM
    @Transactional
    public void deleteItem(UUID id) {

        WarehouseItem item = getItemById(id);

        itemRepository.delete(item);
    }
}