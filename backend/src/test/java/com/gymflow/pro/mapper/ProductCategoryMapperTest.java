package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.request.ProductCategoryRequest;
import com.gymflow.pro.dto.response.ProductCategoryResponse;
import com.gymflow.pro.entity.ProductCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCategoryMapperTest {

    private ProductCategoryMapper categoryMapper;

    @BeforeEach
    void setUp() {
        categoryMapper = new ProductCategoryMapperImpl();
    }

    @Test
    void toEntity_shouldMapName() {
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("Supplements");

        ProductCategory entity = categoryMapper.toEntity(request);

        assertThat(entity.getName()).isEqualTo("Supplements");
    }

    @Test
    void toEntity_shouldReturnNull_whenRequestNull() {
        assertThat(categoryMapper.toEntity(null)).isNull();
    }

    @Test
    void toResponse_shouldMapFields() {
        UUID id = UUID.randomUUID();
        ProductCategory category = ProductCategory.builder().id(id).name("Supplements").build();

        ProductCategoryResponse response = categoryMapper.toResponse(category);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Supplements");
    }

    @Test
    void toResponse_shouldReturnNull_whenCategoryNull() {
        assertThat(categoryMapper.toResponse(null)).isNull();
    }
}
