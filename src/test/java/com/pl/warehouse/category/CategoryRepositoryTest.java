package com.pl.warehouse.category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * STUDY: Persistence slice test.
 *
 * <p>{@code @DataJpaTest} starts only JPA pieces (EntityManager, repositories) + H2.
 * Perfect for testing queries / unique constraints without the full web stack.
 */
@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

	@Autowired
	CategoryRepository categoryRepository;

	@Test
	@DisplayName("save + findAll + existsByNameIgnoreCase")
	void saveAndQuery() {
		Category category = new Category();
		category.setName("Tools");
		category.setDescription("Hand tools");

		Category saved = categoryRepository.save(category);

		assertThat(saved.getId()).isNotNull();
		assertThat(categoryRepository.findAll()).hasSize(1);
		assertThat(categoryRepository.existsByNameIgnoreCase("tools")).isTrue();
		assertThat(categoryRepository.existsByNameIgnoreCase("missing")).isFalse();
	}
}
