package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.StockMovementResponse;
import com.gymflow.pro.entity.Product;
import com.gymflow.pro.entity.StockMovement;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.User;
import com.gymflow.pro.entity.enums.StockMovementReason;
import com.gymflow.pro.entity.enums.StockMovementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StockMovementMapperTest {

    private StockMovementMapper stockMovementMapper;

    @BeforeEach
    void setUp() {
        StockMovementMapperImpl impl = new StockMovementMapperImpl();
        ReflectionTestUtils.setField(impl, "studentMapper", new StudentMapperImpl());
        stockMovementMapper = impl;
    }

    @Test
    void toResponse_shouldMapProductStudentAndCreatedBy_whenPresent() {
        UUID productId = UUID.randomUUID();
        StockMovement movement = StockMovement.builder()
                .id(UUID.randomUUID())
                .product(Product.builder().id(productId).name("Whey").build())
                .type(StockMovementType.EXIT)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(20))
                .reason(StockMovementReason.SALE)
                .student(Student.builder().name("Jane").build())
                .createdBy(User.builder().name("Admin").build())
                .build();

        StockMovementResponse response = stockMovementMapper.toResponse(movement);

        assertThat(response.getProductId()).isEqualTo(productId);
        assertThat(response.getProductName()).isEqualTo("Whey");
        assertThat(response.getStudent().getName()).isEqualTo("Jane");
        assertThat(response.getCreatedByName()).isEqualTo("Admin");
        assertThat(response.getReason()).isEqualTo(StockMovementReason.SALE);
    }

    @Test
    void toResponse_shouldHandleNullProductStudentAndCreatedBy() {
        StockMovement movement = StockMovement.builder()
                .id(UUID.randomUUID())
                .type(StockMovementType.ENTRY)
                .quantity(5)
                .unitPrice(BigDecimal.TEN)
                .reason(StockMovementReason.PURCHASE)
                .build();

        StockMovementResponse response = stockMovementMapper.toResponse(movement);

        assertThat(response.getProductId()).isNull();
        assertThat(response.getProductName()).isNull();
        assertThat(response.getStudent()).isNull();
        assertThat(response.getCreatedByName()).isNull();
    }

    @Test
    void toResponse_shouldReturnNull_whenMovementNull() {
        assertThat(stockMovementMapper.toResponse(null)).isNull();
    }
}
