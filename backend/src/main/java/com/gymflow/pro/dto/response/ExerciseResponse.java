package com.gymflow.pro.dto.response;

import com.gymflow.pro.entity.enums.ExerciseCategory;
import com.gymflow.pro.entity.enums.ExerciseLevel;
import com.gymflow.pro.entity.enums.MuscleGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseResponse {

    private UUID id;
    private String name;
    private String imageUrl;
    private String videoUrl;
    private ExerciseCategory category;
    private MuscleGroup muscleGroup;
    private String equipment;
    private ExerciseLevel level;
    private String instructions;
}
