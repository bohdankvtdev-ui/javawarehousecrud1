package com.pl.warehouse.category;

import com.pl.warehouse.category.dto.CategoryRequest;
import com.pl.warehouse.category.dto.CategoryResponse;
import com.pl.warehouse.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * STUDY: Service unit test — uniqueness rule without hitting the database.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

	@Mock
	CategoryRepository categoryRepository;

	@InjectMocks
	CategoryService categoryService;

	@Test
	@DisplayName("create rejects duplicate names (case-insensitive)")
	void create_duplicateName() {
		when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(true);

		assertThatThrownBy(() ->
				categoryService.create(new CategoryRequest("Electronics", "x"))
		)
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("already exists");
	}

	@Test
	@DisplayName("create persists new category")
	void create_success() {
		when(categoryRepository.existsByNameIgnoreCase("Lighting")).thenReturn(false);
		when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
			Category c = inv.getArgument(0);
			c.setId(UUID.randomUUID());
			return c;
		});

		CategoryResponse response = categoryService.create(new CategoryRequest("Lighting", "Lamps"));

		assertThat(response.name()).isEqualTo("Lighting");
		assertThat(response.id()).isNotNull();
	}

	@Test
	@DisplayName("getById throws when missing")
	void getById_notFound() {
		UUID id = UUID.randomUUID();
		when(categoryRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> categoryService.getById(id))
				.isInstanceOf(ResourceNotFoundException.class);
	}
}
