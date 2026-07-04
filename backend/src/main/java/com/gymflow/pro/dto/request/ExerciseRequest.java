package com.gymflow.pro.dto.request;

import com.gymflow.pro.entity.enums.ExerciseCategory;
import com.gymflow.pro.entity.enums.ExerciseLevel;
import com.gymflow.pro.entity.enums.MuscleGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String imageUrl;
    private String videoUrl;

    @NotNull(message = "Category is required")
    private ExerciseCategory category;

    @NotNull(message = "Muscle group is required")
    private MuscleGroup muscleGroup;

    private String equipment;

    private ExerciseLevel level;

    private String instructions;
}
