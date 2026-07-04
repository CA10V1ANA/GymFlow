package com.gymflow.pro.controller;

import com.gymflow.pro.dto.request.FinancialTransactionRequest;
import com.gymflow.pro.dto.request.MarkAsPaidRequest;
import com.gymflow.pro.dto.response.FinancialTransactionResponse;
import com.gymflow.pro.entity.enums.TransactionCategory;
import com.gymflow.pro.entity.enums.TransactionStatus;
import com.gymflow.pro.entity.enums.TransactionType;
import com.gymflow.pro.service.FinancialService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/financial/transactions")
@RequiredArgsConstructor
@Tag(name = "Financial", description = "Income and expense management")
@PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
public class FinancialController {

    private final FinancialService financialService;

    @GetMapping
    public ResponseEntity<Page<FinancialTransactionResponse>> search(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) TransactionCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        return ResponseEntity.ok(financialService.search(type, status, category, startDate, endDate, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinancialTransactionResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(financialService.findById(id));
    }

    @GetMapping("/cash-flow")
    public ResponseEntity<Map<String, BigDecimal>> cashFlow(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(Map.of("cashFlow", financialService.cashFlow(startDate, endDate)));
    }

    @PostMapping
    public ResponseEntity<FinancialTransactionResponse> create(@Valid @RequestBody FinancialTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financialService.create(request));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<FinancialTransactionResponse> markAsPaid(@PathVariable UUID id, @Valid @RequestBody MarkAsPaidRequest request) {
        return ResponseEntity.ok(financialService.markAsPaid(id, request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<FinancialTransactionResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(financialService.cancel(id));
    }
}
