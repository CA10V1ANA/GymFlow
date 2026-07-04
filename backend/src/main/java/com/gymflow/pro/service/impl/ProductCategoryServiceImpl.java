package com.gymflow.pro.service.impl;

import com.gymflow.pro.dto.request.ProductCategoryRequest;
import com.gymflow.pro.dto.response.ProductCategoryResponse;
import com.gymflow.pro.entity.ProductCategory;
import com.gymflow.pro.exception.BusinessException;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.ProductCategoryMapper;
import com.gymflow.pro.repository.ProductCategoryRepository;
import com.gymflow.pro.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductCategoryMapper categoryMapper;

    @Override
    public List<ProductCategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(categoryMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public ProductCategoryResponse create(ProductCategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException("A category with this name already exists");
        }
        return categoryMapper.toResponse(categoryRepository.save(categoryMapper.toEntity(request)));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("ProductCategory", id));
        categoryRepository.delete(category);
    }
}
