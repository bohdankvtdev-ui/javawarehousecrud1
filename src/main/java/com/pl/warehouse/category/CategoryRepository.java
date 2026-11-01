package com.pl.warehouse.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * STUDY: Spring Data JPA repository for categories.
 * {@code existsByNameIgnoreCase} is a derived query — Spring builds the SQL from the method name.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByNameIgnoreCase(String name);
}
