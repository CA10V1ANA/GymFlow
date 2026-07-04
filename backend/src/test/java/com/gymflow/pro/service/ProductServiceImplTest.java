package com.gymflow.pro.service;

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
import com.gymflow.pro.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCategoryRepository categoryRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private UUID productId;
    private Product product;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        product = Product.builder()
                .id(productId)
                .name("Whey Protein")
                .sku("SKU-1")
                .costPrice(BigDecimal.valueOf(10))
                .salePrice(BigDecimal.valueOf(20))
                .stockQuantity(10)
                .minStock(2)
                .active(true)
                .build();
    }

    @Test
    void findById_shouldReturnProduct_whenExists() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(ProductResponse.builder().id(productId).build());

        ProductResponse response = productService.findById(productId);

        assertThat(response.getId()).isEqualTo(productId);
    }

    @Test
    void findById_shouldThrowResourceNotFoundException_whenMissing() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(productId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldThrowBusinessException_whenSkuAlreadyExists() {
        ProductRequest request = new ProductRequest();
        request.setName("New Product");
        request.setSku("SKU-1");
        request.setCostPrice(BigDecimal.valueOf(5));
        request.setSalePrice(BigDecimal.valueOf(15));
        request.setStockQuantity(1);
        request.setMinStock(0);

        when(productRepository.existsBySku("SKU-1")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("SKU already exists");
        verify(productRepository, never()).save(any());
    }

    @Test
    void create_shouldSaveProduct_whenSkuIsUnique() {
        UUID categoryId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();
        ProductCategory category = ProductCategory.builder().id(categoryId).name("Supplements").build();
        Supplier supplier = Supplier.builder().id(supplierId).name("ACME").build();

        ProductRequest request = new ProductRequest();
        request.setName("New Product");
        request.setSku("SKU-2");
        request.setCostPrice(BigDecimal.valueOf(5));
        request.setSalePrice(BigDecimal.valueOf(15));
        request.setStockQuantity(3);
        request.setMinStock(1);
        request.setCategoryId(categoryId);
        request.setSupplierId(supplierId);
        request.setActive(null);

        when(productRepository.existsBySku("SKU-2")).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productMapper.toResponse(any(Product.class))).thenReturn(ProductResponse.builder().build());

        productService.create(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();
        assertThat(saved.getSku()).isEqualTo("SKU-2");
        assertThat(saved.getCategory()).isEqualTo(category);
        assertThat(saved.getSupplier()).isEqualTo(supplier);
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenCategoryMissing() {
        UUID categoryId = UUID.randomUUID();
        ProductRequest request = new ProductRequest();
        request.setName("New Product");
        request.setSku("SKU-3");
        request.setCostPrice(BigDecimal.valueOf(5));
        request.setSalePrice(BigDecimal.valueOf(15));
        request.setStockQuantity(3);
        request.setMinStock(1);
        request.setCategoryId(categoryId);

        when(productRepository.existsBySku("SKU-3")).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowResourceNotFoundException_whenProductMissing() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(productId, new ProductRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldThrowBusinessException_whenNewSkuAlreadyUsedByAnotherProduct() {
        ProductRequest request = new ProductRequest();
        request.setName("Updated");
        request.setSku("SKU-OTHER");
        request.setCostPrice(BigDecimal.valueOf(5));
        request.setSalePrice(BigDecimal.valueOf(15));
        request.setStockQuantity(3);
        request.setMinStock(1);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySku("SKU-OTHER")).thenReturn(true);

        assertThatThrownBy(() -> productService.update(productId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("SKU already exists");
        verify(productRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateFields_whenSkuUnchanged() {
        ProductRequest request = new ProductRequest();
        request.setName("Updated Name");
        request.setSku("SKU-1");
        request.setCostPrice(BigDecimal.valueOf(11));
        request.setSalePrice(BigDecimal.valueOf(22));
        request.setStockQuantity(5);
        request.setMinStock(1);
        request.setActive(false);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productMapper.toResponse(any(Product.class))).thenReturn(ProductResponse.builder().build());

        productService.update(productId, request);

        verify(productRepository, never()).existsBySku(any());
        assertThat(product.getName()).isEqualTo("Updated Name");
        assertThat(product.getCostPrice()).isEqualByComparingTo(BigDecimal.valueOf(11));
        assertThat(product.getSalePrice()).isEqualByComparingTo(BigDecimal.valueOf(22));
        assertThat(product.getStockQuantity()).isEqualTo(5);
        assertThat(product.isActive()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    void delete_shouldDeactivateProduct_insteadOfRemovingIt() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.delete(productId);

        assertThat(product.isActive()).isFalse();
        verify(productRepository).save(product);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void delete_shouldThrowResourceNotFoundException_whenMissing() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(productId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void lowStockAlerts_shouldReturnMappedLowStockProducts() {
        Product lowStockProduct = Product.builder().id(UUID.randomUUID()).stockQuantity(1).minStock(5).build();
        when(productRepository.findLowStockProducts()).thenReturn(List.of(lowStockProduct));
        when(productMapper.toResponse(lowStockProduct)).thenReturn(ProductResponse.builder().lowStock(true).build());

        List<ProductResponse> result = productService.lowStockAlerts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isLowStock()).isTrue();
    }
}
