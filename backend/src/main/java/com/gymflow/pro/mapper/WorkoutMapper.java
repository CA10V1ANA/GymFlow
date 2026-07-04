package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.WorkoutExerciseResponse;
import com.gymflow.pro.dto.response.WorkoutResponse;
import com.gymflow.pro.entity.Workout;
import com.gymflow.pro.entity.WorkoutExercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {StudentMapper.class, UserMapper.class, ExerciseMapper.class})
public interface WorkoutMapper {

    @Mapping(target = "student", source = "student")
    @Mapping(target = "instructor", source = "instructor")
    WorkoutResponse toResponse(Workout workout);

    @Mapping(target = "exercise", source = "exercise")
    WorkoutExerciseResponse toResponse(WorkoutExercise workoutExercise);
}
