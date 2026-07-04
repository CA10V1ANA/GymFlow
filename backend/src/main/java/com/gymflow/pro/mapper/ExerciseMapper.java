package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.request.ExerciseRequest;
import com.gymflow.pro.dto.response.ExerciseResponse;
import com.gymflow.pro.entity.Exercise;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExerciseMapper {

    Exercise toEntity(ExerciseRequest request);

    ExerciseResponse toResponse(Exercise exercise);

    void updateEntity(ExerciseRequest request, @MappingTarget Exercise exercise);
}
