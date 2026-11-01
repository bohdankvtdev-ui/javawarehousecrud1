package com.pl.warehouse.item;

import com.pl.warehouse.category.Category;
import com.pl.warehouse.category.CategoryRepository;
import com.pl.warehouse.common.api.PageResponse;
import com.pl.warehouse.common.exception.ResourceNotFoundException;
import com.pl.warehouse.item.dto.ItemRequest;
import com.pl.warehouse.item.dto.ItemResponse;
import com.pl.warehouse.supplier.Supplier;
import com.pl.warehouse.supplier.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * STUDY: Unit-test business rules with mocked repositories.
 * {@link ItemMapper} is a {@code @Spy} real object — mapping is simple and pure.
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

	@Mock
	ItemRepository itemRepository;

	@Mock
	CategoryRepository categoryRepository;

	@Mock
	SupplierRepository supplierRepository;

	@Spy
	ItemMapper itemMapper = new ItemMapper();

	@InjectMocks
	ItemService itemService;

	UUID categoryId;
	UUID supplierId;
	Category category;
	Supplier supplier;

	@BeforeEach
	void setUp() {
		categoryId = UUID.randomUUID();
		supplierId = UUID.randomUUID();

		category = new Category();
		category.setId(categoryId);
		category.setName("Electronics");

		supplier = new Supplier();
		supplier.setId(supplierId);
		supplier.setName("TechSource Europe");
	}

	@Test
	@DisplayName("createItem saves ACTIVE item when category and supplier exist")
	void createItem_success() {
		ItemRequest request = new ItemRequest(
				"USB-C Cable",
				"Cable",
				"Study example",
				categoryId,
				supplierId,
				new BigDecimal("7.50"),
				new BigDecimal("0.10")
		);

		when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
		when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
		when(itemRepository.save(any(WarehouseItem.class))).thenAnswer(invocation -> {
			WarehouseItem saved = invocation.getArgument(0);
			saved.setId(UUID.randomUUID());
			return saved;
		});

		ItemResponse response = itemService.createItem(request);

		assertThat(response.itemName()).isEqualTo("USB-C Cable");
		assertThat(response.categoryName()).isEqualTo("Electronics");
		assertThat(response.status()).isEqualTo("ACTIVE");

		ArgumentCaptor<WarehouseItem> captor = ArgumentCaptor.forClass(WarehouseItem.class);
		verify(itemRepository).save(captor.capture());
		assertThat(captor.getValue().getStatus()).isEqualTo(ItemStatus.ACTIVE);
	}

	@Test
	@DisplayName("createItem throws when category is missing")
	void createItem_categoryMissing() {
		ItemRequest request = new ItemRequest(
				"X", "T", null, categoryId, supplierId, BigDecimal.ONE, null
		);
		when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> itemService.createItem(request))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Category");
	}

	@Test
	@DisplayName("getItemById throws when item does not exist")
	void getItemById_notFound() {
		UUID id = UUID.randomUUID();
		when(itemRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> itemService.getItemById(id))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@DisplayName("searchItems maps paged entities to PageResponse")
	@SuppressWarnings("unchecked")
	void searchItems() {
		WarehouseItem item = new WarehouseItem();
		item.setId(UUID.randomUUID());
		item.setItemName("Monitor");
		item.setType("Display");
		item.setCategory(category);
		item.setSupplier(supplier);
		item.setPrice(new BigDecimal("200.00"));
		item.setWeight(new BigDecimal("4.00"));
		item.setStatus(ItemStatus.ACTIVE);

		Pageable pageable = PageRequest.of(0, 10);
		when(itemRepository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(item), pageable, 1));

		PageResponse<ItemResponse> result =
				itemService.searchItems("mon", ItemStatus.ACTIVE, null, pageable);

		assertThat(result.content()).hasSize(1);
		assertThat(result.totalElements()).isEqualTo(1);
	}
}
