package com.pl.warehouse.category;

import com.pl.warehouse.category.dto.CategoryRequest;
import com.pl.warehouse.category.dto.CategoryResponse;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(
              @RequestBody CategoryRequest request) {

        return categoryService.create(request);
    }
}