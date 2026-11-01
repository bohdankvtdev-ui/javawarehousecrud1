package com.pl.warehouse.category;

import com.pl.warehouse.category.dto.CategoryRequest;
import com.pl.warehouse.category.dto.CategoryResponse;
import com.pl.warehouse.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * STUDY: Business logic for the category lookup table used when creating warehouse items.
 * Uniqueness checked here (friendly error) in addition to the DB unique constraint.
 *
 * <p>{@code @Transactional} marks the DB boundary; {@code readOnly = true} on getters
 * is an optimization hint for read-only work.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /** {@code readOnly}: no writes; list for Item form dropdowns. */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID id) {
        return toResponse(find(id));
    }

    /** {@code @Transactional}: uniqueness check + insert as one unit of work. */
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException("Category already exists: " + request.name());
        }

        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = find(id);
        category.setName(request.name());
        category.setDescription(request.description());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID id) {
        categoryRepository.delete(find(id));
    }

    private Category find(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
