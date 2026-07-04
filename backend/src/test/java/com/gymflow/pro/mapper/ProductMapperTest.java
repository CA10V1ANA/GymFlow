package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.ProductResponse;
import com.gymflow.pro.entity.Product;
import com.gymflow.pro.entity.ProductCategory;
import com.gymflow.pro.entity.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        ProductMapperImpl impl = new ProductMapperImpl();
        ReflectionTestUtils.setField(impl, "productCategoryMapper", new ProductCategoryMapperImpl());
        ReflectionTestUtils.setField(impl, "supplierMapper", new SupplierMapperImpl());
        productMapper = impl;
    }

    @Test
    void toResponse_shouldMapFieldsAndComputeLowStock_whenBelowMinimum() {
        UUID id = UUID.randomUUID();
        Product product = Product.builder()
                .id(id)
                .name("Whey")
                .sku("SKU-1")
                .costPrice(BigDecimal.valueOf(10))
                .salePrice(BigDecimal.valueOf(20))
                .stockQuantity(2)
                .minStock(5)
                .category(ProductCategory.builder().name("Supplements").build())
                .supplier(Supplier.builder().name("Acme").build())
                .active(true)
                .build();

        ProductResponse response = productMapper.toResponse(product);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Whey");
        assertThat(response.isLowStock()).isTrue();
        assertThat(response.getCategory().getName()).isEqualTo("Supplements");
        assertThat(response.getSupplier().getName()).isEqualTo("Acme");
    }

    @Test
    void toResponse_shouldComputeLowStock_false_whenAboveMinimum() {
        Product product = Product.builder()
                .name("Whey")
                .stockQuantity(50)
                .minStock(5)
                .build();

        ProductResponse response = productMapper.toResponse(product);

        assertThat(response.isLowStock()).isFalse();
    }

    @Test
    void toResponse_shouldReturnNull_whenProductNull() {
        assertThat(productMapper.toResponse(null)).isNull();
    }
}
