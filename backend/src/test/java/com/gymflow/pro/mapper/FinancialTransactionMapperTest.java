package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.FinancialTransactionResponse;
import com.gymflow.pro.entity.Enrollment;
import com.gymflow.pro.entity.FinancialTransaction;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.TransactionCategory;
import com.gymflow.pro.entity.enums.TransactionStatus;
import com.gymflow.pro.entity.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FinancialTransactionMapperTest {

    private FinancialTransactionMapper financialTransactionMapper;

    @BeforeEach
    void setUp() {
        FinancialTransactionMapperImpl impl = new FinancialTransactionMapperImpl();
        ReflectionTestUtils.setField(impl, "studentMapper", new StudentMapperImpl());
        financialTransactionMapper = impl;
    }

    @Test
    void toResponse_shouldComputeNetAmount_andMapEnrollmentId() {
        UUID enrollmentId = UUID.randomUUID();
        FinancialTransaction transaction = FinancialTransaction.builder()
                .id(UUID.randomUUID())
                .type(TransactionType.INCOME)
                .category(TransactionCategory.MONTHLY_FEE)
                .description("Monthly fee")
                .amount(BigDecimal.valueOf(150))
                .discount(BigDecimal.valueOf(10))
                .penalty(BigDecimal.valueOf(5))
                .status(TransactionStatus.PENDING)
                .dueDate(LocalDate.now())
                .student(Student.builder().name("Jane").build())
                .enrollment(Enrollment.builder().id(enrollmentId).build())
                .build();

        FinancialTransactionResponse response = financialTransactionMapper.toResponse(transaction);

        assertThat(response.getNetAmount()).isEqualByComparingTo(BigDecimal.valueOf(145));
        assertThat(response.getEnrollmentId()).isEqualTo(enrollmentId);
        assertThat(response.getStudent().getName()).isEqualTo("Jane");
    }

    @Test
    void toResponse_shouldHandleNullEnrollment() {
        FinancialTransaction transaction = FinancialTransaction.builder()
                .amount(BigDecimal.valueOf(100))
                .discount(BigDecimal.ZERO)
                .penalty(BigDecimal.ZERO)
                .dueDate(LocalDate.now())
                .build();

        FinancialTransactionResponse response = financialTransactionMapper.toResponse(transaction);

        assertThat(response.getEnrollmentId()).isNull();
        assertThat(response.getNetAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void toResponse_shouldReturnNull_whenTransactionNull() {
        assertThat(financialTransactionMapper.toResponse(null)).isNull();
    }
}
