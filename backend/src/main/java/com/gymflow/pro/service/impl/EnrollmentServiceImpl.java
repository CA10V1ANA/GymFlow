package com.gymflow.pro.service.impl;

import com.gymflow.pro.dto.request.EnrollmentRequest;
import com.gymflow.pro.dto.response.EnrollmentResponse;
import com.gymflow.pro.entity.Enrollment;
import com.gymflow.pro.entity.FinancialTransaction;
import com.gymflow.pro.entity.Plan;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.EnrollmentStatus;
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
import com.gymflow.pro.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final PlanRepository planRepository;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final EnrollmentMapper enrollmentMapper;

    @Override
    public Page<EnrollmentResponse> findAll(Pageable pageable) {
        return enrollmentRepository.findAll(pageable).map(enrollmentMapper::toResponse);
    }

    @Override
    public EnrollmentResponse findById(UUID id) {
        return enrollmentMapper.toResponse(getEnrollmentOrThrow(id));
    }

    @Override
    @Transactional
    public EnrollmentResponse create(EnrollmentRequest request) {
        Student student = getStudentOrThrow(request.getStudentId());
        Plan plan = getPlanOrThrow(request.getPlanId());

        enrollmentRepository.findFirstByStudentAndStatusOrderByCreatedAtDesc(student, EnrollmentStatus.ACTIVE)
                .ifPresent(e -> {
                    throw new BusinessException("Student already has an active enrollment");
                });

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        Enrollment enrollment = buildEnrollment(student, plan, startDate, request.getPricePaid());

        Enrollment saved = enrollmentRepository.save(enrollment);
        createMonthlyFeeTransaction(saved);
        return enrollmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EnrollmentResponse renew(UUID id, EnrollmentRequest request) {
        Enrollment previous = getEnrollmentOrThrow(id);
        Plan plan = request.getPlanId() != null ? getPlanOrThrow(request.getPlanId()) : previous.getPlan();

        LocalDate startDate = previous.getEndDate().isAfter(LocalDate.now()) ? previous.getEndDate() : LocalDate.now();
        Enrollment renewed = buildEnrollment(previous.getStudent(), plan, startDate, request.getPricePaid());

        previous.setStatus(EnrollmentStatus.EXPIRED);
        enrollmentRepository.save(previous);

        Enrollment saved = enrollmentRepository.save(renewed);
        createMonthlyFeeTransaction(saved);
        return enrollmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EnrollmentResponse cancel(UUID id, String reason) {
        Enrollment enrollment = getEnrollmentOrThrow(id);
        if (enrollment.getStatus() == EnrollmentStatus.CANCELED) {
            throw new BusinessException("Enrollment is already canceled");
        }
        enrollment.setStatus(EnrollmentStatus.CANCELED);
        enrollment.setCancelReason(reason);
        enrollment.setCanceledAt(LocalDateTime.now());
        return enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentResponse freeze(UUID id) {
        Enrollment enrollment = getEnrollmentOrThrow(id);
        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new BusinessException("Only active enrollments can be frozen");
        }
        enrollment.setStatus(EnrollmentStatus.FROZEN);
        enrollment.setFrozenSince(LocalDate.now());
        return enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentResponse reactivate(UUID id) {
        Enrollment enrollment = getEnrollmentOrThrow(id);
        if (enrollment.getStatus() != EnrollmentStatus.FROZEN) {
            throw new BusinessException("Only frozen enrollments can be reactivated");
        }
        if (enrollment.getFrozenSince() != null) {
            long frozenDays = java.time.temporal.ChronoUnit.DAYS.between(enrollment.getFrozenSince(), LocalDate.now());
            enrollment.setEndDate(enrollment.getEndDate().plusDays(Math.max(frozenDays, 0)));
        }
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setFrozenSince(null);
        return enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
    }

    private Enrollment buildEnrollment(Student student, Plan plan, LocalDate startDate, BigDecimal pricePaidOverride) {
        BigDecimal price = pricePaidOverride != null ? pricePaidOverride : calculatePrice(plan);
        return Enrollment.builder()
                .student(student)
                .plan(plan)
                .startDate(startDate)
                .endDate(startDate.plusMonths(plan.getDurationMonths()))
                .status(EnrollmentStatus.ACTIVE)
                .pricePaid(price)
                .build();
    }

    private BigDecimal calculatePrice(Plan plan) {
        BigDecimal discountFactor = BigDecimal.ONE.subtract(
                plan.getDiscountPercentage().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return plan.getPrice().multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
    }

    private void createMonthlyFeeTransaction(Enrollment enrollment) {
        FinancialTransaction transaction = FinancialTransaction.builder()
                .type(TransactionType.INCOME)
                .category(TransactionCategory.MONTHLY_FEE)
                .description("Monthly fee - " + enrollment.getPlan().getName() + " - " + enrollment.getStudent().getName())
                .amount(enrollment.getPricePaid())
                .status(TransactionStatus.PENDING)
                .dueDate(enrollment.getStartDate())
                .student(enrollment.getStudent())
                .enrollment(enrollment)
                .build();
        financialTransactionRepository.save(transaction);
    }

    private Enrollment getEnrollmentOrThrow(UUID id) {
        return enrollmentRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Enrollment", id));
    }

    private Student getStudentOrThrow(UUID id) {
        return studentRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Student", id));
    }

    private Plan getPlanOrThrow(UUID id) {
        return planRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Plan", id));
    }
}
