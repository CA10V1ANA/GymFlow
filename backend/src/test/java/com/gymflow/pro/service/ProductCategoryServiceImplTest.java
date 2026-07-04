package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.ProductCategoryRequest;
import com.gymflow.pro.dto.response.ProductCategoryResponse;
import com.gymflow.pro.entity.ProductCategory;
import com.gymflow.pro.exception.BusinessException;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.ProductCategoryMapper;
import com.gymflow.pro.repository.ProductCategoryRepository;
import com.gymflow.pro.service.impl.ProductCategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceImplTest {

    @Mock
    private ProductCategoryRepository categoryRepository;

    @Mock
    private ProductCategoryMapper categoryMapper;

    private ProductCategoryServiceImpl categoryService;

    private ProductCategory category;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryService = new ProductCategoryServiceImpl(categoryRepository, categoryMapper);
        categoryId = UUID.randomUUID();
        category = ProductCategory.builder().id(categoryId).name("Supplements").build();
    }

    @Test
    void findAll_shouldReturnMappedList() {
        ProductCategoryResponse response = ProductCategoryResponse.builder().id(categoryId).name("Supplements").build();
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        List<ProductCategoryResponse> result = categoryService.findAll();

        assertThat(result).containsExactly(response);
    }

    @Test
    void create_shouldSaveAndReturnMappedResponse_whenNameAvailable() {
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("Supplements");
        ProductCategory mappedEntity = ProductCategory.builder().name("Supplements").build();
        ProductCategoryResponse response = ProductCategoryResponse.builder().id(categoryId).name("Supplements").build();

        when(categoryRepository.existsByNameIgnoreCase("Supplements")).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(mappedEntity);
        when(categoryRepository.save(mappedEntity)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        ProductCategoryResponse result = categoryService.create(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void create_shouldThrowBusinessException_whenNameAlreadyExists() {
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("Supplements");

        when(categoryRepository.existsByNameIgnoreCase("Supplements")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(BusinessException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void delete_shouldRemoveCategory_whenExists() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        categoryService.delete(categoryId);

        verify(categoryRepository).delete(category);
    }

    @Test
    void delete_shouldThrowResourceNotFound_whenMissing() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(categoryId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).delete(any());
    }
}
