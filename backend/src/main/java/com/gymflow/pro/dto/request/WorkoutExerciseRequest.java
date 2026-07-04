package com.gymflow.pro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class WorkoutExerciseRequest {

    @NotNull(message = "Exercise is required")
    private UUID exerciseId;

    private Integer sortOrder;

    @NotNull(message = "Number of sets is required")
    private Integer sets;

    @NotBlank(message = "Repetitions are required")
    private String repetitions;

    private BigDecimal loadKg;
    private Integer durationSeconds;
    private Integer restSeconds;
    private String notes;
}
