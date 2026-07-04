package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.EnrollmentRequest;
import com.gymflow.pro.dto.response.EnrollmentResponse;
import com.gymflow.pro.entity.Enrollment;
import com.gymflow.pro.entity.FinancialTransaction;
import com.gymflow.pro.entity.Plan;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.EnrollmentStatus;
import com.gymflow.pro.entity.enums.PlanType;
import com.gymflow.pro.entity.enums.TransactionCategory;
import com.gymflow.pro.entity.enums.TransactionStatus;
import com.gymflow.pro.entity.enums.TransactionType;
import com.gymflow.pro.exception.BusinessException;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.EnrollmentMapper;
import com.gymflow.pro.repository.EnrollmentRepository;
import com.gymflow.pro.repository.FinancialTransactionRepository;
import com.gymflow.pro.repository.PlanRepository;
import com.gymflow.pro.repository.StudentRepository;
import com.gymflow.pro.service.impl.EnrollmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private FinancialTransactionRepository financialTransactionRepository;

    @Mock
    private EnrollmentMapper enrollmentMapper;

    private EnrollmentServiceImpl enrollmentService;

    private Student student;
    private Plan plan;
    private UUID studentId;
    private UUID planId;

    @BeforeEach
    void setUp() {
        enrollmentService = new EnrollmentServiceImpl(enrollmentRepository, studentRepository, planRepository,
                financialTransactionRepository, enrollmentMapper);
        studentId = UUID.randomUUID();
        planId = UUID.randomUUID();
        student = Student.builder().id(studentId).name("John Doe").build();
        plan = Plan.builder().id(planId).name("Gold").type(PlanType.QUARTERLY).durationMonths(3)
                .price(BigDecimal.valueOf(300)).discountPercentage(BigDecimal.ZERO).active(true).build();
    }

    private EnrollmentRequest buildRequest() {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(studentId);
        request.setPlanId(planId);
        return request;
    }

    @Test
    void findAll_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Enrollment enrollment = Enrollment.builder().id(UUID.randomUUID()).build();
        Page<Enrollment> page = new PageImpl<>(List.of(enrollment));
        EnrollmentResponse response = EnrollmentResponse.builder().id(enrollment.getId()).build();

        when(enrollmentRepository.findAll(pageable)).thenReturn(page);
        when(enrollmentMapper.toResponse(enrollment)).thenReturn(response);

        Page<EnrollmentResponse> result = enrollmentService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void findById_shouldThrowResourceNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(enrollmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldThrowResourceNotFound_whenStudentMissing() {
        EnrollmentRequest request = buildRequest();
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(planRepository, never()).findById(any());
    }

    @Test
    void create_shouldThrowResourceNotFound_whenPlanMissing() {
        EnrollmentRequest request = buildRequest();
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldThrowBusinessException_whenStudentHasActiveEnrollment() {
        EnrollmentRequest request = buildRequest();
        Enrollment activeEnrollment = Enrollment.builder().id(UUID.randomUUID()).status(EnrollmentStatus.ACTIVE).build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(enrollmentRepository.findFirstByStudentAndStatusOrderByCreatedAtDesc(student, EnrollmentStatus.ACTIVE))
                .thenReturn(Optional.of(activeEnrollment));

        assertThatThrownBy(() -> enrollmentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already has an active enrollment");

        verify(enrollmentRepository, never()).save(any());
        verify(financialTransactionRepository, never()).save(any());
    }

    @Test
    void create_shouldComputeEndDateFromPlanDuration_andCreatePendingTransaction() {
        EnrollmentRequest request = buildRequest();
        LocalDate startDate = LocalDate.of(2026, 1, 10);
        request.setStartDate(startDate);
        EnrollmentResponse response = EnrollmentResponse.builder().id(UUID.randomUUID()).build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(enrollmentRepository.findFirstByStudentAndStatusOrderByCreatedAtDesc(student, EnrollmentStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(enrollmentMapper.toResponse(any(Enrollment.class))).thenReturn(response);

        EnrollmentResponse result = enrollmentService.create(request);

        assertThat(result).isEqualTo(response);

        ArgumentCaptor<Enrollment> enrollmentCaptor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(enrollmentCaptor.capture());
        Enrollment saved = enrollmentCaptor.getValue();
        assertThat(saved.getStartDate()).isEqualTo(startDate);
        assertThat(saved.getEndDate()).isEqualTo(startDate.plusMonths(plan.getDurationMonths()));
        assertThat(saved.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(saved.getPricePaid()).isEqualByComparingTo(plan.getPrice());

        ArgumentCaptor<FinancialTransaction> transactionCaptor = ArgumentCaptor.forClass(FinancialTransaction.class);
        verify(financialTransactionRepository).save(transactionCaptor.capture());
        FinancialTransaction transaction = transactionCaptor.getValue();
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(transaction.getCategory()).isEqualTo(TransactionCategory.MONTHLY_FEE);
        assertThat(transaction.getDueDate()).isEqualTo(startDate);
        assertThat(transaction.getStudent()).isEqualTo(student);
        assertThat(transaction.getEnrollment()).isEqualTo(saved);
        assertThat(transaction.getAmount()).isEqualByComparingTo(plan.getPrice());
    }

    @Test
    void create_shouldDefaultStartDateToToday_whenNotProvided() {
        EnrollmentRequest request = buildRequest();
        EnrollmentResponse response = EnrollmentResponse.builder().id(UUID.randomUUID()).build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(enrollmentRepository.findFirstByStudentAndStatusOrderByCreatedAtDesc(student, EnrollmentStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(enrollmentMapper.toResponse(any(Enrollment.class))).thenReturn(response);

        enrollmentService.create(request);

        ArgumentCaptor<Enrollment> enrollmentCaptor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(enrollmentCaptor.capture());
        Enrollment saved = enrollmentCaptor.getValue();
        assertThat(saved.getStartDate()).isEqualTo(LocalDate.now());
        assertThat(saved.getEndDate()).isEqualTo(LocalDate.now().plusMonths(plan.getDurationMonths()));
    }

    @Test
    void create_shouldApplyDiscountPercentage_whenPricePaidNotOverridden() {
        EnrollmentRequest request = buildRequest();
        plan.setDiscountPercentage(BigDecimal.TEN);
        EnrollmentResponse response = EnrollmentResponse.builder().id(UUID.randomUUID()).build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(enrollmentRepository.findFirstByStudentAndStatusOrderByCreatedAtDesc(student, EnrollmentStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(enrollmentMapper.toResponse(any(Enrollment.class))).thenReturn(response);

        enrollmentService.create(request);

        ArgumentCaptor<Enrollment> enrollmentCaptor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(enrollmentCaptor.capture());
        // 300 * (1 - 10/100) = 270.00
        assertThat(enrollmentCaptor.getValue().getPricePaid()).isEqualByComparingTo(BigDecimal.valueOf(270).setScale(2));
    }

    @Test
    void create_shouldUsePricePaidOverride_whenProvided() {
        EnrollmentRequest request = buildRequest();
        request.setPricePaid(BigDecimal.valueOf(123.45));
        EnrollmentResponse response = EnrollmentResponse.builder().id(UUID.randomUUID()).build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(enrollmentRepository.findFirstByStudentAndStatusOrderByCreatedAtDesc(student, EnrollmentStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(enrollmentMapper.toResponse(any(Enrollment.class))).thenReturn(response);

        enrollmentService.create(request);

        ArgumentCaptor<Enrollment> enrollmentCaptor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(enrollmentCaptor.capture());
        assertThat(enrollmentCaptor.getValue().getPricePaid()).isEqualByComparingTo(BigDecimal.valueOf(123.45));
    }

    @Test
    void renew_shouldThrowResourceNotFound_whenPreviousEnrollmentMissing() {
        UUID id = UUID.randomUUID();
        EnrollmentRequest request = buildRequest();
        when(enrollmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.renew(id, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void renew_shouldExpirePrevious_andCreateNewEnrollmentStartingFromPreviousEndDate_whenStillActive() {
        UUID previousId = UUID.randomUUID();
        LocalDate previousEnd = LocalDate.now().plusDays(10);
        Enrollment previous = Enrollment.builder().id(previousId).student(student).plan(plan)
                .startDate(previousEnd.minusMonths(plan.getDurationMonths())).endDate(previousEnd)
                .status(EnrollmentStatus.ACTIVE).pricePaid(plan.getPrice()).build();
        EnrollmentRequest request = new EnrollmentRequest();
        request.setPlanId(planId);
        EnrollmentResponse response = EnrollmentResponse.builder().id(UUID.randomUUID()).build();

        when(enrollmentRepository.findById(previousId)).thenReturn(Optional.of(previous));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(enrollmentMapper.toResponse(any(Enrollment.class))).thenReturn(response);

        EnrollmentResponse result = enrollmentService.renew(previousId, request);

        assertThat(result).isEqualTo(response);
        assertThat(previous.getStatus()).isEqualTo(EnrollmentStatus.EXPIRED);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository, times(2)).save(captor.capture());
        List<Enrollment> savedEnrollments = captor.getAllValues();
        Enrollment renewed = savedEnrollments.get(1);
        assertThat(renewed.getStartDate()).isEqualTo(previousEnd);
        assertThat(renewed.getEndDate()).isEqualTo(previousEnd.plusMonths(plan.getDurationMonths()));
        assertThat(renewed.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        verify(financialTransactionRepository).save(any(FinancialTransaction.class));
    }

    @Test
    void renew_shouldStartFromToday_whenPreviousEnrollmentAlreadyExpired() {
        UUID previousId = UUID.randomUUID();
        LocalDate previousEnd = LocalDate.now().minusDays(5);
        Enrollment previous = Enrollment.builder().id(previousId).student(student).plan(plan)
                .startDate(previousEnd.minusMonths(plan.getDurationMonths())).endDate(previousEnd)
                .status(EnrollmentStatus.EXPIRED).pricePaid(plan.getPrice()).build();
        EnrollmentRequest request = new EnrollmentRequest();
        EnrollmentResponse response = EnrollmentResponse.builder().id(UUID.randomUUID()).build();

        when(enrollmentRepository.findById(previousId)).thenReturn(Optional.of(previous));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(enrollmentMapper.toResponse(any(Enrollment.class))).thenReturn(response);

        enrollmentService.renew(previousId, request);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository, times(2)).save(captor.capture());
        Enrollment renewed = captor.getAllValues().get(1);
        assertThat(renewed.getStartDate()).isEqualTo(LocalDate.now());
        assertThat(renewed.getPlan()).isEqualTo(plan);
    }

    @Test
    void cancel_shouldSetStatusAndReason() {
        UUID id = UUID.randomUUID();
        Enrollment enrollment = Enrollment.builder().id(id).status(EnrollmentStatus.ACTIVE).build();
        EnrollmentResponse response = EnrollmentResponse.builder().id(id).build();

        when(enrollmentRepository.findById(id)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);
        when(enrollmentMapper.toResponse(enrollment)).thenReturn(response);

        EnrollmentResponse result = enrollmentService.cancel(id, "no longer interested");

        assertThat(result).isEqualTo(response);
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELED);
        assertThat(enrollment.getCancelReason()).isEqualTo("no longer interested");
        assertThat(enrollment.getCanceledAt()).isNotNull();
    }

    @Test
    void cancel_shouldThrowBusinessException_whenAlreadyCanceled() {
        UUID id = UUID.randomUUID();
        Enrollment enrollment = Enrollment.builder().id(id).status(EnrollmentStatus.CANCELED).build();
        when(enrollmentRepository.findById(id)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> enrollmentService.cancel(id, "reason"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already canceled");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void freeze_shouldSetStatusFrozen_andRecordFrozenSince_whenActive() {
        UUID id = UUID.randomUUID();
        Enrollment enrollment = Enrollment.builder().id(id).status(EnrollmentStatus.ACTIVE).build();
        EnrollmentResponse response = EnrollmentResponse.builder().id(id).build();

        when(enrollmentRepository.findById(id)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);
        when(enrollmentMapper.toResponse(enrollment)).thenReturn(response);

        EnrollmentResponse result = enrollmentService.freeze(id);

        assertThat(result).isEqualTo(response);
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.FROZEN);
        assertThat(enrollment.getFrozenSince()).isEqualTo(LocalDate.now());
    }

    @Test
    void freeze_shouldThrowBusinessException_whenNotActive() {
        UUID id = UUID.randomUUID();
        Enrollment enrollment = Enrollment.builder().id(id).status(EnrollmentStatus.FROZEN).build();
        when(enrollmentRepository.findById(id)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> enrollmentService.freeze(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only active enrollments");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void reactivate_shouldExtendEndDateByFrozenDays_andSetActive() {
        UUID id = UUID.randomUUID();
        LocalDate frozenSince = LocalDate.now().minusDays(7);
        LocalDate originalEnd = LocalDate.now().plusDays(30);
        Enrollment enrollment = Enrollment.builder().id(id).status(EnrollmentStatus.FROZEN)
                .frozenSince(frozenSince).endDate(originalEnd).build();
        EnrollmentResponse response = EnrollmentResponse.builder().id(id).build();

        when(enrollmentRepository.findById(id)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);
        when(enrollmentMapper.toResponse(enrollment)).thenReturn(response);

        EnrollmentResponse result = enrollmentService.reactivate(id);

        assertThat(result).isEqualTo(response);
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(enrollment.getFrozenSince()).isNull();
        assertThat(enrollment.getEndDate()).isEqualTo(originalEnd.plusDays(7));
    }

    @Test
    void reactivate_shouldThrowBusinessException_whenNotFrozen() {
        UUID id = UUID.randomUUID();
        Enrollment enrollment = Enrollment.builder().id(id).status(EnrollmentStatus.ACTIVE).build();
        when(enrollmentRepository.findById(id)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> enrollmentService.reactivate(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only frozen enrollments");

        verify(enrollmentRepository, never()).save(any());
    }
}
