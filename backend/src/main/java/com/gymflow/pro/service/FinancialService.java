package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.FinancialTransactionRequest;
import com.gymflow.pro.dto.request.MarkAsPaidRequest;
import com.gymflow.pro.dto.response.FinancialTransactionResponse;
import com.gymflow.pro.entity.enums.TransactionCategory;
import com.gymflow.pro.entity.enums.TransactionStatus;
import com.gymflow.pro.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface FinancialService {

    Page<FinancialTransactionResponse> search(TransactionType type, TransactionStatus status,
                                               TransactionCategory category, LocalDate startDate, LocalDate endDate,
                                               Pageable pageable);

    FinancialTransactionResponse findById(UUID id);

    FinancialTransactionResponse create(FinancialTransactionRequest request);

    FinancialTransactionResponse markAsPaid(UUID id, MarkAsPaidRequest request);

    FinancialTransactionResponse cancel(UUID id);

    BigDecimal cashFlow(LocalDate startDate, LocalDate endDate);

    void refreshOverdueStatuses();
}
