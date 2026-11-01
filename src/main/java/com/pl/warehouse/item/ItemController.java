package com.pl.warehouse.item;

import com.pl.warehouse.item.dto.ItemRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // GET http://localhost:8080/api/items
    @GetMapping
    public List<WarehouseItem> getAllItems() {
        return itemService.getAllItems();
    }

    // GET http://localhost:8080/api/items/{id}
    @GetMapping("/{id}")
    public WarehouseItem getItemById(@PathVariable UUID id) {
        return itemService.getItemById(id);
    }

    // POST http://localhost:8080/api/items
    @PostMapping
    public WarehouseItem createItem(@RequestBody ItemRequest request) {
        return itemService.createItem(request);
    }

    // DELETE http://localhost:8080/api/items/{id}
    @DeleteMapping("/{id}")
    public String deleteItem(@PathVariable UUID id) {
        itemService.deleteItem(id);
        return "Item deleted successfully";
    }
}