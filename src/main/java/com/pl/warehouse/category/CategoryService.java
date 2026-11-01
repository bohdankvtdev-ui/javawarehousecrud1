package com.pl.warehouse.category;

import com.pl.warehouse.category.dto.CategoryRequest;
import com.pl.warehouse.category.dto.CategoryResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAll() {

        return categoryRepository.findAll()
                .stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                ))
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {

        Category category = new Category();

        category.setName(request.name());
        category.setDescription(request.description());

        Category saved = categoryRepository.save(category);

        return new CategoryResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription()
        );
    }
}