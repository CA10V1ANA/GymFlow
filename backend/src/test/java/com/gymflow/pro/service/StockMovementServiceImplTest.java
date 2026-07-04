package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.StockMovementRequest;
import com.gymflow.pro.dto.response.StockMovementResponse;
import com.gymflow.pro.entity.Product;
import com.gymflow.pro.entity.StockMovement;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.User;
import com.gymflow.pro.entity.enums.StockMovementReason;
import com.gymflow.pro.entity.enums.StockMovementType;
import com.gymflow.pro.exception.BusinessException;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.StockMovementMapper;
import com.gymflow.pro.repository.ProductRepository;
import com.gymflow.pro.repository.StockMovementRepository;
import com.gymflow.pro.repository.StudentRepository;
import com.gymflow.pro.repository.UserRepository;
import com.gymflow.pro.service.impl.StockMovementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
class StockMovementServiceImplTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockMovementMapper stockMovementMapper;

    @InjectMocks
    private StockMovementServiceImpl stockMovementService;

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
    void findByProduct_shouldReturnMappedPage_whenProductExists() {
        Pageable pageable = PageRequest.of(0, 10);
        StockMovement movement = StockMovement.builder().id(UUID.randomUUID()).product(product).build();
        Page<StockMovement> page = new PageImpl<>(List.of(movement));

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockMovementRepository.findByProductOrderByCreatedAtDesc(product, pageable)).thenReturn(page);
        when(stockMovementMapper.toResponse(movement)).thenReturn(StockMovementResponse.builder().productId(productId).build());

        Page<StockMovementResponse> result = stockMovementService.findByProduct(productId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProductId()).isEqualTo(productId);
    }

    @Test
    void findByProduct_shouldThrowResourceNotFoundException_whenProductMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockMovementService.findByProduct(productId, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void register_shouldIncreaseStock_forEntryMovement() {
        StockMovementRequest request = new StockMovementRequest();
        request.setProductId(productId);
        request.setType(StockMovementType.ENTRY);
        request.setQuantity(5);
        request.setUnitPrice(BigDecimal.valueOf(10));
        request.setReason(StockMovementReason.PURCHASE);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementMapper.toResponse(any(StockMovement.class))).thenReturn(StockMovementResponse.builder().build());

        stockMovementService.register(request, null);

        assertThat(product.getStockQuantity()).isEqualTo(15);
        verify(productRepository).save(product);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void register_shouldDecreaseStock_forExitMovement_whenSufficientStock() {
        StockMovementRequest request = new StockMovementRequest();
        request.setProductId(productId);
        request.setType(StockMovementType.EXIT);
        request.setQuantity(4);
        request.setUnitPrice(BigDecimal.valueOf(20));
        request.setReason(StockMovementReason.SALE);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementMapper.toResponse(any(StockMovement.class))).thenReturn(StockMovementResponse.builder().build());

        stockMovementService.register(request, null);

        assertThat(product.getStockQuantity()).isEqualTo(6);
    }

    @Test
    void register_shouldThrowBusinessException_whenExitExceedsAvailableStock() {
        StockMovementRequest request = new StockMovementRequest();
        request.setProductId(productId);
        request.setType(StockMovementType.EXIT);
        request.setQuantity(999);
        request.setUnitPrice(BigDecimal.valueOf(20));
        request.setReason(StockMovementReason.SALE);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> stockMovementService.register(request, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock");

        verify(productRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void register_shouldThrowResourceNotFoundException_whenProductMissing() {
        StockMovementRequest request = new StockMovementRequest();
        request.setProductId(productId);
        request.setType(StockMovementType.ENTRY);
        request.setQuantity(1);
        request.setUnitPrice(BigDecimal.valueOf(1));
        request.setReason(StockMovementReason.PURCHASE);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockMovementService.register(request, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void register_shouldThrowResourceNotFoundException_whenStudentMissing() {
        UUID studentId = UUID.randomUUID();
        StockMovementRequest request = new StockMovementRequest();
        request.setProductId(productId);
        request.setType(StockMovementType.ENTRY);
        request.setQuantity(1);
        request.setUnitPrice(BigDecimal.valueOf(1));
        request.setReason(StockMovementReason.PURCHASE);
        request.setStudentId(studentId);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockMovementService.register(request, null))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void register_shouldResolveCreatedByUser_whenEmailProvidedAndFound() {
        String email = "staff@example.com";
        User user = User.builder().id(UUID.randomUUID()).email(email).build();

        StockMovementRequest request = new StockMovementRequest();
        request.setProductId(productId);
        request.setType(StockMovementType.ENTRY);
        request.setQuantity(2);
        request.setUnitPrice(BigDecimal.valueOf(5));
        request.setReason(StockMovementReason.ADJUSTMENT);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementMapper.toResponse(any(StockMovement.class))).thenReturn(StockMovementResponse.builder().build());

        stockMovementService.register(request, email);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedBy()).isEqualTo(user);
    }

    @Test
    void register_shouldLeaveCreatedByNull_whenEmailProvidedButUserNotFound() {
        String email = "missing@example.com";

        StockMovementRequest request = new StockMovementRequest();
        request.setProductId(productId);
        request.setType(StockMovementType.ENTRY);
        request.setQuantity(2);
        request.setUnitPrice(BigDecimal.valueOf(5));
        request.setReason(StockMovementReason.ADJUSTMENT);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementMapper.toResponse(any(StockMovement.class))).thenReturn(StockMovementResponse.builder().build());

        stockMovementService.register(request, email);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedBy()).isNull();
    }
}
