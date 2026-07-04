package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.FinancialTransactionRequest;
import com.gymflow.pro.dto.request.MarkAsPaidRequest;
import com.gymflow.pro.dto.response.FinancialTransactionResponse;
import com.gymflow.pro.entity.Enrollment;
import com.gymflow.pro.entity.FinancialTransaction;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.PaymentMethod;
import com.gymflow.pro.entity.enums.TransactionCategory;
import com.gymflow.pro.entity.enums.TransactionStatus;
import com.gymflow.pro.entity.enums.TransactionType;
import com.gymflow.pro.exception.BusinessException;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.FinancialTransactionMapper;
import com.gymflow.pro.repository.EnrollmentRepository;
import com.gymflow.pro.repository.FinancialTransactionRepository;
import com.gymflow.pro.repository.StudentRepository;
import com.gymflow.pro.service.impl.FinancialServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialServiceImplTest {

    @Mock
    private FinancialTransactionRepository transactionRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private FinancialTransactionMapper transactionMapper;

    @InjectMocks
    private FinancialServiceImpl financialService;

    private UUID transactionId;
    private FinancialTransaction transaction;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        transaction = FinancialTransaction.builder()
                .id(transactionId)
                .type(TransactionType.INCOME)
                .category(TransactionCategory.MONTHLY_FEE)
                .description("Monthly fee")
                .amount(BigDecimal.valueOf(100))
                .discount(BigDecimal.ZERO)
                .penalty(BigDecimal.ZERO)
                .status(TransactionStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(5))
                .build();
    }

    @Test
    void findById_shouldReturnTransaction_whenExists() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toResponse(transaction)).thenReturn(FinancialTransactionResponse.builder().id(transactionId).build());

        FinancialTransactionResponse response = financialService.findById(transactionId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(transactionId);
    }

    @Test
    void findById_shouldThrowResourceNotFoundException_whenMissing() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.findById(transactionId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldBuildPendingTransaction_withoutStudentOrEnrollment() {
        FinancialTransactionRequest request = new FinancialTransactionRequest();
        request.setType(TransactionType.EXPENSE);
        request.setCategory(TransactionCategory.RENT);
        request.setDescription("Rent");
        request.setAmount(BigDecimal.valueOf(500));
        request.setDueDate(LocalDate.now().plusDays(10));

        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionMapper.toResponse(any(FinancialTransaction.class)))
                .thenAnswer(inv -> FinancialTransactionResponse.builder()
                        .status(((FinancialTransaction) inv.getArgument(0)).getStatus())
                        .build());

        FinancialTransactionResponse response = financialService.create(request);

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.PENDING);

        ArgumentCaptor<FinancialTransaction> captor = ArgumentCaptor.forClass(FinancialTransaction.class);
        verify(transactionRepository).save(captor.capture());
        FinancialTransaction saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(saved.getDiscount()).isEqualTo(BigDecimal.ZERO);
        assertThat(saved.getPenalty()).isEqualTo(BigDecimal.ZERO);
        assertThat(saved.getStudent()).isNull();
        assertThat(saved.getEnrollment()).isNull();
        verify(studentRepository, never()).findById(any());
        verify(enrollmentRepository, never()).findById(any());
    }

    @Test
    void create_shouldAttachStudentAndEnrollment_whenIdsProvided() {
        UUID studentId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        Student student = Student.builder().id(studentId).build();
        Enrollment enrollment = Enrollment.builder().id(enrollmentId).build();

        FinancialTransactionRequest request = new FinancialTransactionRequest();
        request.setType(TransactionType.INCOME);
        request.setCategory(TransactionCategory.MONTHLY_FEE);
        request.setDescription("Fee");
        request.setAmount(BigDecimal.valueOf(150));
        request.setDueDate(LocalDate.now().plusDays(5));
        request.setStudentId(studentId);
        request.setEnrollmentId(enrollmentId);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionMapper.toResponse(any(FinancialTransaction.class))).thenReturn(FinancialTransactionResponse.builder().build());

        financialService.create(request);

        ArgumentCaptor<FinancialTransaction> captor = ArgumentCaptor.forClass(FinancialTransaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getStudent()).isEqualTo(student);
        assertThat(captor.getValue().getEnrollment()).isEqualTo(enrollment);
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenStudentMissing() {
        UUID studentId = UUID.randomUUID();
        FinancialTransactionRequest request = new FinancialTransactionRequest();
        request.setType(TransactionType.INCOME);
        request.setCategory(TransactionCategory.MONTHLY_FEE);
        request.setDescription("Fee");
        request.setAmount(BigDecimal.valueOf(150));
        request.setDueDate(LocalDate.now().plusDays(5));
        request.setStudentId(studentId);

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void markAsPaid_shouldSetPaidStatus_whenPending() {
        MarkAsPaidRequest request = new MarkAsPaidRequest();
        request.setPaymentMethod(PaymentMethod.PIX);

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionMapper.toResponse(any(FinancialTransaction.class)))
                .thenAnswer(inv -> FinancialTransactionResponse.builder()
                        .status(((FinancialTransaction) inv.getArgument(0)).getStatus())
                        .paymentMethod(((FinancialTransaction) inv.getArgument(0)).getPaymentMethod())
                        .build());

        FinancialTransactionResponse response = financialService.markAsPaid(transactionId, request);

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.PAID);
        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.PIX);
        assertThat(transaction.getPaidAt()).isNotNull();
        verify(transactionRepository).save(transaction);
    }

    @Test
    void markAsPaid_shouldThrowBusinessException_whenAlreadyPaid() {
        transaction.setStatus(TransactionStatus.PAID);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> financialService.markAsPaid(transactionId, new MarkAsPaidRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already paid");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void markAsPaid_shouldThrowBusinessException_whenCanceled() {
        transaction.setStatus(TransactionStatus.CANCELED);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> financialService.markAsPaid(transactionId, new MarkAsPaidRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot pay");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void markAsPaid_shouldThrowResourceNotFoundException_whenMissing() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.markAsPaid(transactionId, new MarkAsPaidRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancel_shouldSetCanceledStatus_whenNotPaid() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionMapper.toResponse(any(FinancialTransaction.class)))
                .thenAnswer(inv -> FinancialTransactionResponse.builder()
                        .status(((FinancialTransaction) inv.getArgument(0)).getStatus())
                        .build());

        FinancialTransactionResponse response = financialService.cancel(transactionId);

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.CANCELED);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void cancel_shouldThrowBusinessException_whenAlreadyPaid() {
        transaction.setStatus(TransactionStatus.PAID);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> financialService.cancel(transactionId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already paid");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void cashFlow_shouldReturnIncomeMinusExpense() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        when(transactionRepository.sumPaidByTypeAndPeriod(eq(TransactionType.INCOME), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.valueOf(1000));
        when(transactionRepository.sumPaidByTypeAndPeriod(eq(TransactionType.EXPENSE), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.valueOf(300));

        BigDecimal result = financialService.cashFlow(start, end);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(700));
    }

    @Test
    void refreshOverdueStatuses_shouldMarkPendingOverdueTransactionsAsOverdue() {
        FinancialTransaction overdue1 = FinancialTransaction.builder().id(UUID.randomUUID()).status(TransactionStatus.PENDING).build();
        FinancialTransaction overdue2 = FinancialTransaction.builder().id(UUID.randomUUID()).status(TransactionStatus.PENDING).build();
        List<FinancialTransaction> overdueList = List.of(overdue1, overdue2);

        when(transactionRepository.findByStatusAndDueDateBefore(eq(TransactionStatus.PENDING), any(LocalDate.class)))
                .thenReturn(overdueList);

        financialService.refreshOverdueStatuses();

        assertThat(overdue1.getStatus()).isEqualTo(TransactionStatus.OVERDUE);
        assertThat(overdue2.getStatus()).isEqualTo(TransactionStatus.OVERDUE);
        verify(transactionRepository, times(1)).saveAll(overdueList);
    }
}
