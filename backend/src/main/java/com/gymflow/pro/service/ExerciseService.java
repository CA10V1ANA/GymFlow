package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.ExerciseRequest;
import com.gymflow.pro.dto.response.ExerciseResponse;
import com.gymflow.pro.entity.enums.ExerciseCategory;
import com.gymflow.pro.entity.enums.ExerciseLevel;
import com.gymflow.pro.entity.enums.MuscleGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ExerciseService {

    Page<ExerciseResponse> search(ExerciseCategory category, MuscleGroup muscleGroup, ExerciseLevel level,
                                   String search, Pageable pageable);

    ExerciseResponse findById(UUID id);

    ExerciseResponse create(ExerciseRequest request);

    ExerciseResponse update(UUID id, ExerciseRequest request);

    void delete(UUID id);
}
