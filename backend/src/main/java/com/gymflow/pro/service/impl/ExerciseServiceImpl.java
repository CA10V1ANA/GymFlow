package com.gymflow.pro.service.impl;

import com.gymflow.pro.dto.request.ExerciseRequest;
import com.gymflow.pro.dto.response.ExerciseResponse;
import com.gymflow.pro.entity.Exercise;
import com.gymflow.pro.entity.enums.ExerciseCategory;
import com.gymflow.pro.entity.enums.ExerciseLevel;
import com.gymflow.pro.entity.enums.MuscleGroup;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.ExerciseMapper;
import com.gymflow.pro.repository.ExerciseRepository;
import com.gymflow.pro.repository.specification.ExerciseSpecifications;
import com.gymflow.pro.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    @Override
    public Page<ExerciseResponse> search(ExerciseCategory category, MuscleGroup muscleGroup, ExerciseLevel level,
                                          String search, Pageable pageable) {
        return exerciseRepository.findAll(ExerciseSpecifications.withFilters(category, muscleGroup, level, search), pageable)
                .map(exerciseMapper::toResponse);
    }

    @Override
    public ExerciseResponse findById(UUID id) {
        return exerciseMapper.toResponse(getExerciseOrThrow(id));
    }

    @Override
    @Transactional
    public ExerciseResponse create(ExerciseRequest request) {
        Exercise exercise = exerciseMapper.toEntity(request);
        return exerciseMapper.toResponse(exerciseRepository.save(exercise));
    }

    @Override
    @Transactional
    public ExerciseResponse update(UUID id, ExerciseRequest request) {
        Exercise exercise = getExerciseOrThrow(id);
        exerciseMapper.updateEntity(request, exercise);
        return exerciseMapper.toResponse(exerciseRepository.save(exercise));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        exerciseRepository.delete(getExerciseOrThrow(id));
    }

    private Exercise getExerciseOrThrow(UUID id) {
        return exerciseRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Exercise", id));
    }
}
