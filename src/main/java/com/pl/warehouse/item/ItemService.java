package com.pl.warehouse.item;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class  ItemService {
    private final  ItemRepository itemRepository;

    @Autowired
    public  ItemService(ItemRepository itemRepository){
        this.itemRepository = itemRepository;
    }

    public List<WarehouseItem> getWarehouseItems(){
        List<WarehouseItem> items = itemRepository.findAll();

        System.out.println(items+"ffererf");
        return itemRepository.findAll();

    }

    public List<WarehouseItem> getWarehouseItemsFromQuantity(String searchText){
        return itemRepository.findAll().stream()
                .filter(item->searchText.equals(Integer.valueOf(item.getQuantity())))
                .collect(Collectors.toList());
    }

    public List<WarehouseItem> getWarehouseItemsByName(String searchText){
        return itemRepository.findAll().stream()
                .filter(item->item.getName().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
    }
    public List<WarehouseItem> getWarehouseItemsByType(String searchText){
        return itemRepository.findAll().stream()
                .filter(item->
                        item.getType().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<WarehouseItem> getWarehouseItemsByCategory(String searchText){
        return itemRepository.findAll().stream()
                .filter(item->
                        item.getCategory().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<WarehouseItem> getWarehouseItemsByTypeAndCategory(String team, String position){
        return itemRepository.findAll().stream()
                .filter(item->team.equals(item.getType()) && position.equals(item.getCategory()))
                .collect(Collectors.toList());
    }
    public WarehouseItem addWarehouseItem(WarehouseItem item){
        itemRepository.save(item);
        return item;
    }
    public WarehouseItem updateWarehouseItem(WarehouseItem updatedWarehouseItem){
        Optional<WarehouseItem> existingWarehouseItem  = itemRepository.findByName(updatedWarehouseItem.getName());
        if(existingWarehouseItem.isPresent()){
            WarehouseItem itemToUpdate = existingWarehouseItem.get();
            itemToUpdate.setName(updatedWarehouseItem.getName());
            itemToUpdate.setType(updatedWarehouseItem.getType());
            itemToUpdate.setCategory(updatedWarehouseItem.getCategory());
            itemToUpdate.setQuantity(updatedWarehouseItem.getQuantity());

            itemRepository.save(itemToUpdate);
            return itemToUpdate;
        }
        return null;
    }

    @Transactional
    public void deleteWarehouseItem(String itemName){
        itemRepository.deleteByName(itemName);
    }
}
