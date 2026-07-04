package com.gymflow.pro.service.impl;

import com.gymflow.pro.dto.request.FinancialTransactionRequest;
import com.gymflow.pro.dto.request.MarkAsPaidRequest;
import com.gymflow.pro.dto.response.FinancialTransactionResponse;
import com.gymflow.pro.entity.Enrollment;
import com.gymflow.pro.entity.FinancialTransaction;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.TransactionCategory;
import com.gymflow.pro.entity.enums.TransactionStatus;
import com.gymflow.pro.entity.enums.TransactionType;
import com.gymflow.pro.exception.BusinessException;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.FinancialTransactionMapper;
import com.gymflow.pro.repository.EnrollmentRepository;
import com.gymflow.pro.repository.FinancialTransactionRepository;
import com.gymflow.pro.repository.StudentRepository;
import com.gymflow.pro.repository.specification.FinancialTransactionSpecifications;
import com.gymflow.pro.service.FinancialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialServiceImpl implements FinancialService {

    private final FinancialTransactionRepository transactionRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FinancialTransactionMapper transactionMapper;

    @Override
    public Page<FinancialTransactionResponse> search(TransactionType type, TransactionStatus status,
                                                       TransactionCategory category, LocalDate startDate,
                                                       LocalDate endDate, Pageable pageable) {
        return transactionRepository.findAll(
                FinancialTransactionSpecifications.withFilters(type, status, category, startDate, endDate), pageable)
                .map(transactionMapper::toResponse);
    }

    @Override
    public FinancialTransactionResponse findById(UUID id) {
        return transactionMapper.toResponse(getTransactionOrThrow(id));
    }

    @Override
    @Transactional
    public FinancialTransactionResponse create(FinancialTransactionRequest request) {
        FinancialTransaction.FinancialTransactionBuilder builder = FinancialTransaction.builder()
                .type(request.getType())
                .category(request.getCategory())
                .description(request.getDescription())
                .amount(request.getAmount())
                .discount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO)
                .penalty(request.getPenalty() != null ? request.getPenalty() : BigDecimal.ZERO)
                .paymentMethod(request.getPaymentMethod())
                .status(TransactionStatus.PENDING)
                .dueDate(request.getDueDate());

        if (request.getStudentId() != null) {
            Student student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Student", request.getStudentId()));
            builder.student(student);
        }
        if (request.getEnrollmentId() != null) {
            Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Enrollment", request.getEnrollmentId()));
            builder.enrollment(enrollment);
        }

        return transactionMapper.toResponse(transactionRepository.save(builder.build()));
    }

    @Override
    @Transactional
    public FinancialTransactionResponse markAsPaid(UUID id, MarkAsPaidRequest request) {
        FinancialTransaction transaction = getTransactionOrThrow(id);
        if (transaction.getStatus() == TransactionStatus.PAID) {
            throw new BusinessException("Transaction is already paid");
        }
        if (transaction.getStatus() == TransactionStatus.CANCELED) {
            throw new BusinessException("Cannot pay a canceled transaction");
        }
        transaction.setStatus(TransactionStatus.PAID);
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setPaidAt(LocalDateTime.now());
        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public FinancialTransactionResponse cancel(UUID id) {
        FinancialTransaction transaction = getTransactionOrThrow(id);
        if (transaction.getStatus() == TransactionStatus.PAID) {
            throw new BusinessException("Cannot cancel a transaction that is already paid");
        }
        transaction.setStatus(TransactionStatus.CANCELED);
        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Override
    public BigDecimal cashFlow(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        BigDecimal income = transactionRepository.sumPaidByTypeAndPeriod(TransactionType.INCOME, start, end);
        BigDecimal expense = transactionRepository.sumPaidByTypeAndPeriod(TransactionType.EXPENSE, start, end);
        return income.subtract(expense);
    }

    @Override
    @Transactional
    public void refreshOverdueStatuses() {
        List<FinancialTransaction> pendingOverdue = transactionRepository
                .findByStatusAndDueDateBefore(TransactionStatus.PENDING, LocalDate.now());
        pendingOverdue.forEach(t -> t.setStatus(TransactionStatus.OVERDUE));
        transactionRepository.saveAll(pendingOverdue);
    }

    private FinancialTransaction getTransactionOrThrow(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("FinancialTransaction", id));
    }
}
