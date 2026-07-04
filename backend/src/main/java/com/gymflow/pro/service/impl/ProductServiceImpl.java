package com.gymflow.pro.service.impl;

import com.gymflow.pro.dto.request.ProductRequest;
import com.gymflow.pro.dto.response.ProductResponse;
import com.gymflow.pro.entity.Product;
import com.gymflow.pro.entity.ProductCategory;
import com.gymflow.pro.entity.Supplier;
import com.gymflow.pro.exception.BusinessException;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.ProductMapper;
import com.gymflow.pro.repository.ProductCategoryRepository;
import com.gymflow.pro.repository.ProductRepository;
import com.gymflow.pro.repository.SupplierRepository;
import com.gymflow.pro.repository.specification.ProductSpecifications;
import com.gymflow.pro.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductResponse> search(String search, UUID categoryId, Boolean active, Boolean lowStock, Pageable pageable) {
        return productRepository.findAll(ProductSpecifications.withFilters(search, categoryId, active, lowStock), pageable)
                .map(productMapper::toResponse);
    }

    @Override
    public ProductResponse findById(UUID id) {
        return productMapper.toResponse(getProductOrThrow(id));
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("A product with this SKU already exists");
        }
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(resolveCategory(request.getCategoryId()))
                .supplier(resolveSupplier(request.getSupplierId()))
                .sku(request.getSku())
                .costPrice(request.getCostPrice())
                .salePrice(request.getSalePrice())
                .stockQuantity(request.getStockQuantity())
                .minStock(request.getMinStock())
                .active(request.getActive() == null || request.getActive())
                .build();
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = getProductOrThrow(id);
        if (!product.getSku().equals(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("A product with this SKU already exists");
        }
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(resolveCategory(request.getCategoryId()));
        product.setSupplier(resolveSupplier(request.getSupplierId()));
        product.setSku(request.getSku());
        product.setCostPrice(request.getCostPrice());
        product.setSalePrice(request.getSalePrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setMinStock(request.getMinStock());
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Product product = getProductOrThrow(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    public List<ProductResponse> lowStockAlerts() {
        return productRepository.findLowStockProducts().stream().map(productMapper::toResponse).toList();
    }

    private ProductCategory resolveCategory(UUID id) {
        if (id == null) {
            return null;
        }
        return categoryRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("ProductCategory", id));
    }

    private Supplier resolveSupplier(UUID id) {
        if (id == null) {
            return null;
        }
        return supplierRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Supplier", id));
    }

    private Product getProductOrThrow(UUID id) {
        return productRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Product", id));
    }
}
