package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.ProductRequest;
import com.gymflow.pro.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    Page<ProductResponse> search(String search, UUID categoryId, Boolean active, Boolean lowStock, Pageable pageable);

    ProductResponse findById(UUID id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(UUID id, ProductRequest request);

    void delete(UUID id);

    List<ProductResponse> lowStockAlerts();
}
