package com.gymflow.pro.dto.response;

import com.gymflow.pro.entity.enums.PaymentMethod;
import com.gymflow.pro.entity.enums.TransactionCategory;
import com.gymflow.pro.entity.enums.TransactionStatus;
import com.gymflow.pro.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialTransactionResponse {

    private UUID id;
    private TransactionType type;
    private TransactionCategory category;
    private String description;
    private BigDecimal amount;
    private BigDecimal discount;
    private BigDecimal penalty;
    private BigDecimal netAmount;
    private PaymentMethod paymentMethod;
    private TransactionStatus status;
    private LocalDate dueDate;
    private LocalDateTime paidAt;
    private StudentSummaryResponse student;
    private UUID enrollmentId;
    private LocalDateTime createdAt;
}
