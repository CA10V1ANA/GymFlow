package com.gymflow.pro.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class EnrollmentRequest {

    @NotNull(message = "Student is required")
    private UUID studentId;

    @NotNull(message = "Plan is required")
    private UUID planId;

    private LocalDate startDate;

    /** Optional override; if absent the plan's price (minus discount) is used. */
    private BigDecimal pricePaid;
}
