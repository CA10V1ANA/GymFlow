package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.request.ExerciseRequest;
import com.gymflow.pro.dto.response.ExerciseResponse;
import com.gymflow.pro.entity.Exercise;
import com.gymflow.pro.entity.enums.ExerciseCategory;
import com.gymflow.pro.entity.enums.ExerciseLevel;
import com.gymflow.pro.entity.enums.MuscleGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExerciseMapperTest {

    private ExerciseMapper exerciseMapper;

    @BeforeEach
    void setUp() {
        exerciseMapper = new ExerciseMapperImpl();
    }

    private ExerciseRequest buildRequest() {
        ExerciseRequest request = new ExerciseRequest();
        request.setName("Squat");
        request.setCategory(ExerciseCategory.STRENGTH);
        request.setMuscleGroup(MuscleGroup.LEGS);
        request.setLevel(ExerciseLevel.INTERMEDIATE);
        request.setEquipment("Barbell");
        return request;
    }

    @Test
    void toEntity_shouldMapAllFields() {
        Exercise entity = exerciseMapper.toEntity(buildRequest());

        assertThat(entity.getName()).isEqualTo("Squat");
        assertThat(entity.getCategory()).isEqualTo(ExerciseCategory.STRENGTH);
        assertThat(entity.getMuscleGroup()).isEqualTo(MuscleGroup.LEGS);
        assertThat(entity.getLevel()).isEqualTo(ExerciseLevel.INTERMEDIATE);
        assertThat(entity.getEquipment()).isEqualTo("Barbell");
    }

    @Test
    void toEntity_shouldReturnNull_whenRequestNull() {
        assertThat(exerciseMapper.toEntity(null)).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        Exercise exercise = Exercise.builder()
                .id(id)
                .name("Squat")
                .category(ExerciseCategory.STRENGTH)
                .muscleGroup(MuscleGroup.LEGS)
                .level(ExerciseLevel.INTERMEDIATE)
                .build();

        ExerciseResponse response = exerciseMapper.toResponse(exercise);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Squat");
        assertThat(response.getCategory()).isEqualTo(ExerciseCategory.STRENGTH);
    }

    @Test
    void toResponse_shouldReturnNull_whenExerciseNull() {
        assertThat(exerciseMapper.toResponse(null)).isNull();
    }

    @Test
    void updateEntity_shouldOnlyOverwriteNonNullFields() {
        Exercise exercise = Exercise.builder().name("Old").equipment("Old Equipment").build();
        ExerciseRequest request = new ExerciseRequest();
        request.setName("New");

        exerciseMapper.updateEntity(request, exercise);

        assertThat(exercise.getName()).isEqualTo("New");
        assertThat(exercise.getEquipment()).isEqualTo("Old Equipment");
    }

    @Test
    void updateEntity_shouldDoNothing_whenRequestNull() {
        Exercise exercise = Exercise.builder().name("Old").build();

        exerciseMapper.updateEntity(null, exercise);

        assertThat(exercise.getName()).isEqualTo("Old");
    }
}
