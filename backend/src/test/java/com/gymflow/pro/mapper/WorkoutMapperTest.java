package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.WorkoutExerciseResponse;
import com.gymflow.pro.dto.response.WorkoutResponse;
import com.gymflow.pro.entity.Exercise;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.User;
import com.gymflow.pro.entity.Workout;
import com.gymflow.pro.entity.WorkoutExercise;
import com.gymflow.pro.entity.enums.ExerciseCategory;
import com.gymflow.pro.entity.enums.MuscleGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutMapperTest {

    private WorkoutMapper workoutMapper;

    @BeforeEach
    void setUp() {
        WorkoutMapperImpl impl = new WorkoutMapperImpl();
        ReflectionTestUtils.setField(impl, "studentMapper", new StudentMapperImpl());
        ReflectionTestUtils.setField(impl, "userMapper", new UserMapperImpl());
        ReflectionTestUtils.setField(impl, "exerciseMapper", new ExerciseMapperImpl());
        workoutMapper = impl;
    }

    @Test
    void toResponse_workout_shouldMapNestedStudentInstructorAndExercises() {
        UUID id = UUID.randomUUID();
        Exercise exercise = Exercise.builder().name("Squat").category(ExerciseCategory.STRENGTH).muscleGroup(MuscleGroup.LEGS).build();
        WorkoutExercise workoutExercise = WorkoutExercise.builder().exercise(exercise).sets(3).repetitions("10").build();
        Workout workout = Workout.builder()
                .id(id)
                .student(Student.builder().name("Jane").build())
                .instructor(User.builder().name("Coach").build())
                .name("Full Body")
                .active(true)
                .exercises(List.of(workoutExercise))
                .build();

        WorkoutResponse response = workoutMapper.toResponse(workout);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getStudent().getName()).isEqualTo("Jane");
        assertThat(response.getInstructor().getName()).isEqualTo("Coach");
        assertThat(response.getExercises()).hasSize(1);
        assertThat(response.getExercises().get(0).getExercise().getName()).isEqualTo("Squat");
    }

    @Test
    void toResponse_workout_shouldReturnNull_whenWorkoutNull() {
        assertThat(workoutMapper.toResponse((Workout) null)).isNull();
    }

    @Test
    void toResponse_workoutExercise_shouldMapFields() {
        Exercise exercise = Exercise.builder().name("Deadlift").build();
        WorkoutExercise workoutExercise = WorkoutExercise.builder()
                .exercise(exercise)
                .sortOrder(1)
                .sets(4)
                .repetitions("8")
                .build();

        WorkoutExerciseResponse response = workoutMapper.toResponse(workoutExercise);

        assertThat(response.getExercise().getName()).isEqualTo("Deadlift");
        assertThat(response.getSets()).isEqualTo(4);
        assertThat(response.getRepetitions()).isEqualTo("8");
    }

    @Test
    void toResponse_workoutExercise_shouldReturnNull_whenNull() {
        assertThat(workoutMapper.toResponse((WorkoutExercise) null)).isNull();
    }

    @Test
    void toResponse_workout_shouldHandleNullExerciseList() {
        Workout workout = Workout.builder()
                .student(Student.builder().name("Jane").build())
                .name("Full Body")
                .exercises(null)
                .build();

        WorkoutResponse response = workoutMapper.toResponse(workout);

        assertThat(response.getExercises()).isNull();
    }
}
