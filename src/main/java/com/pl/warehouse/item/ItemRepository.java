package com.pl.warehouse.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<WarehouseItem,String> {

    void deleteByName(String ItemName);
    Optional<WarehouseItem> findByName(String name);

}
