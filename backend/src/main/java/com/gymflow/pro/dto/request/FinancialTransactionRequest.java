package com.gymflow.pro.dto.request;

import com.gymflow.pro.entity.enums.PaymentMethod;
import com.gymflow.pro.entity.enums.TransactionCategory;
import com.gymflow.pro.entity.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class FinancialTransactionRequest {

    @NotNull(message = "Type is required")
    private TransactionType type;

    @NotNull(message = "Category is required")
    private TransactionCategory category;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
    private BigDecimal amount;

    private BigDecimal discount;
    private BigDecimal penalty;

    private PaymentMethod paymentMethod;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private UUID studentId;
    private UUID enrollmentId;
}
