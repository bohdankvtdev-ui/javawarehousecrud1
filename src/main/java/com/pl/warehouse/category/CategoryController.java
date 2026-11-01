package com.pl.warehouse.category;

import com.pl.warehouse.category.dto.CategoryRequest;
import com.pl.warehouse.category.dto.CategoryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * STUDY — Thin HTTP adapter for category CRUD.
 * Categories feed the Item create/edit form. {@code @Valid} validates requests;
 * {@code @ResponseStatus} sets CREATED (201) / NO_CONTENT (204). No SQL here.
 * Frontend route: {@code /categories}.
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** List all categories (public GET). */
    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryService.getAll();
    }

    /** Get one category by id. Missing → 404 via GlobalExceptionHandler. */
    @GetMapping("/{id}")
    public CategoryResponse getById(@PathVariable UUID id) {
        return categoryService.getById(id);
    }

    /** Create category (USER/ADMIN). Duplicate names rejected in the service. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    /** Update category name/description. */
    @PutMapping("/{id}")
    public CategoryResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request
    ) {
        return categoryService.update(id, request);
    }

    /** Delete category (ADMIN). May fail if items still reference it (FK). */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        categoryService.delete(id);
    }
}
