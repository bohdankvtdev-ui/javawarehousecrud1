package com.pl.warehouse.item;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="warehouse_items")
public class WarehouseItem {
    @Id
    @Column(name = "item_name",unique=true, nullable = false)
    private String name;

    private String type;
    private String category;
    private String description;

    private Integer quantity;
    private Integer minQuantity;

    private Double price;
    private Double weight;

    private String location;
    private String supplier;
    private String status;

    private String warehouseSection;

    public WarehouseItem(){

    }

    public WarehouseItem(String name) {
        this.name = name;
    }

    public WarehouseItem(String name, String type, String category, String description, Integer quantity, Integer minQuantity, Double price, Double weight, String location, String supplier, String status, String warehouseSection) {
        this.name = name;
        this.type = type;
        this.category = category;
        this.description = description;
        this.quantity = quantity;
        this.minQuantity = minQuantity;
        this.price = price;
        this.weight = weight;
        this.location = location;
        this.supplier = supplier;
        this.status = status;
        this.warehouseSection = warehouseSection;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWarehouseSection() {
        return warehouseSection;
    }

    public void setWarehouseSection(String warehouseSection) {
        this.warehouseSection = warehouseSection;
    }
}
