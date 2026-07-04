package com.gymflow.pro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutExerciseResponse {

    private UUID id;
    private ExerciseResponse exercise;
    private Integer sortOrder;
    private Integer sets;
    private String repetitions;
    private BigDecimal loadKg;
    private Integer durationSeconds;
    private Integer restSeconds;
    private String notes;
}
