package com.gymflow.pro.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class WorkoutRequest {

    @NotNull(message = "Student is required")
    private UUID studentId;

    private UUID instructorId;

    @NotBlank(message = "Name is required")
    private String name;

    private String goal;

    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
    private Boolean active;

    @NotEmpty(message = "At least one exercise is required")
    @Valid
    private List<WorkoutExerciseRequest> exercises;
}
