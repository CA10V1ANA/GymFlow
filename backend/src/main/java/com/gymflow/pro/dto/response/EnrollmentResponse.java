package com.gymflow.pro.dto.response;

import com.gymflow.pro.entity.enums.EnrollmentStatus;
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
public class EnrollmentResponse {

    private UUID id;
    private StudentSummaryResponse student;
    private PlanResponse plan;
    private LocalDate startDate;
    private LocalDate endDate;
    private EnrollmentStatus status;
    private LocalDate frozenSince;
    private BigDecimal pricePaid;
    private LocalDateTime canceledAt;
    private String cancelReason;
    private LocalDateTime createdAt;
}
