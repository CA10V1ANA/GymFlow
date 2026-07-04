package com.gymflow.pro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutResponse {

    private UUID id;
    private StudentSummaryResponse student;
    private UserResponse instructor;
    private String name;
    private String goal;
    private boolean active;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
    private List<WorkoutExerciseResponse> exercises;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
