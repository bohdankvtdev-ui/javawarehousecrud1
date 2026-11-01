package com.pl.warehouse.item;

import com.pl.warehouse.common.api.PageResponse;
import com.pl.warehouse.common.exception.GlobalExceptionHandler;
import com.pl.warehouse.common.exception.ResourceNotFoundException;
import com.pl.warehouse.config.SecurityConfig;
import com.pl.warehouse.item.dto.ItemRequest;
import com.pl.warehouse.item.dto.ItemResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class ItemControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	ItemService itemService;

	@Test
	@DisplayName("GET /api/v1/items is public and returns PageResponse")
	void searchItems_public() throws Exception {
		UUID id = UUID.randomUUID();
		when(itemService.searchItems(isNull(), isNull(), isNull(), any(Pageable.class)))
				.thenReturn(new PageResponse<>(
						List.of(new ItemResponse(
								id, "Cable", "Cable", "desc",
								UUID.randomUUID(), "Electronics",
								UUID.randomUUID(), "TechSource",
								new BigDecimal("7.50"), new BigDecimal("0.1"), "ACTIVE"
						)),
						0, 10, 1, 1, true, true
				));

		mockMvc.perform(get("/api/v1/items"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].itemName").value("Cable"))
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	@DisplayName("POST without auth returns 401")
	void createItem_unauthorized() throws Exception {
		mockMvc.perform(post("/api/v1/items")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "itemName": "Cable",
								  "type": "Cable",
								  "categoryId": "d0000000-0000-4000-8000-000000000001",
								  "supplierId": "c0000000-0000-4000-8000-000000000001",
								  "price": 7.50
								}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "USER")
	@DisplayName("POST with USER role returns 201")
	void createItem_created() throws Exception {
		UUID categoryId = UUID.randomUUID();
		UUID supplierId = UUID.randomUUID();

		when(itemService.createItem(any(ItemRequest.class))).thenReturn(
				new ItemResponse(
						UUID.randomUUID(), "Cable", "Cable", "desc",
						categoryId, "Electronics", supplierId, "TechSource",
						new BigDecimal("7.50"), new BigDecimal("0.1"), "ACTIVE"
				)
		);

		String body = """
				{
				  "itemName": "Cable",
				  "type": "Cable",
				  "description": "desc",
				  "categoryId": "%s",
				  "supplierId": "%s",
				  "price": 7.50,
				  "weight": 0.1
				}
				""".formatted(categoryId, supplierId);

		mockMvc.perform(post("/api/v1/items")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.itemName").value("Cable"));
	}

	@Test
	@WithMockUser(roles = "USER")
	@DisplayName("POST validation error returns 400")
	void createItem_validationError() throws Exception {
		mockMvc.perform(post("/api/v1/items")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	@DisplayName("GET missing item returns 404")
	void getById_notFound() throws Exception {
		UUID id = UUID.randomUUID();
		when(itemService.getItemById(id))
				.thenThrow(new ResourceNotFoundException("Item", id));

		mockMvc.perform(get("/api/v1/items/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	@WithMockUser(roles = "USER")
	@DisplayName("DELETE with USER role is forbidden")
	void deleteItem_forbiddenForUser() throws Exception {
		mockMvc.perform(delete("/api/v1/items/{id}", UUID.randomUUID()).with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("DELETE with ADMIN role returns 204")
	void deleteItem_admin() throws Exception {
		mockMvc.perform(delete("/api/v1/items/{id}", UUID.randomUUID()).with(csrf()))
				.andExpect(status().isNoContent());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("DELETE missing item returns 404")
	void deleteItem_notFound() throws Exception {
		UUID id = UUID.randomUUID();
		doThrow(new ResourceNotFoundException("Item", id))
				.when(itemService).deleteItem(eq(id));

		mockMvc.perform(delete("/api/v1/items/{id}", id).with(csrf()))
				.andExpect(status().isNotFound());
	}
}
