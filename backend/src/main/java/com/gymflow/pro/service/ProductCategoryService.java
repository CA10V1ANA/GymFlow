package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.ProductCategoryRequest;
import com.gymflow.pro.dto.response.ProductCategoryResponse;

import java.util.List;
import java.util.UUID;

public interface ProductCategoryService {

    List<ProductCategoryResponse> findAll();

    ProductCategoryResponse create(ProductCategoryRequest request);

    void delete(UUID id);
}
