package com.pl.warehouse.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path="api/v1/item")
public class  ItemController {
    private final  ItemService itemService;

    @Autowired
    public  ItemController( ItemService itemService){
        this.itemService = itemService;
    }

    @GetMapping
    public List<WarehouseItem> getWarehouseItems(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String quantity){
            if(type != null && category !=null){
                return itemService.getWarehouseItemsByTypeAndCategory(type, category);
            }
            else if(type!=null){
                return itemService.getWarehouseItemsByType(type);
            }
            else if(category!=null){
                return itemService.getWarehouseItemsByCategory(category);
            }
            else if(quantity!=null){
                return itemService.getWarehouseItemsFromQuantity(quantity);
            }
            else{
                return itemService.getWarehouseItems();
            }
    }

    @PostMapping
    public ResponseEntity<WarehouseItem> addWarehouseItem(@RequestBody WarehouseItem item){
        WarehouseItem createdWarehouseItem  = itemService.addWarehouseItem(item);
        return new ResponseEntity<>(createdWarehouseItem, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<WarehouseItem> updateWarehouseItem(@RequestBody WarehouseItem item){
        WarehouseItem resultWarehouseItem = itemService.updateWarehouseItem(item);
        if(resultWarehouseItem != null){
            return new ResponseEntity<>(resultWarehouseItem, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping("/{itemName}")
    public ResponseEntity<String> deleteWarehouseItem(@PathVariable String itemName){
        itemService.deleteWarehouseItem(itemName);
        return new ResponseEntity<>("WarehouseItem deleted", HttpStatus.OK);
    }


}
