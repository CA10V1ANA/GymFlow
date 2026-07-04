package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.ExerciseRequest;
import com.gymflow.pro.dto.response.ExerciseResponse;
import com.gymflow.pro.entity.Exercise;
import com.gymflow.pro.entity.enums.ExerciseCategory;
import com.gymflow.pro.entity.enums.ExerciseLevel;
import com.gymflow.pro.entity.enums.MuscleGroup;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.ExerciseMapper;
import com.gymflow.pro.repository.ExerciseRepository;
import com.gymflow.pro.service.impl.ExerciseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceImplTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private ExerciseMapper exerciseMapper;

    @InjectMocks
    private ExerciseServiceImpl exerciseService;

    private Exercise exercise;
    private UUID exerciseId;

    @BeforeEach
    void setUp() {
        exerciseId = UUID.randomUUID();
        exercise = Exercise.builder()
                .id(exerciseId)
                .name("Bench Press")
                .category(ExerciseCategory.STRENGTH)
                .muscleGroup(MuscleGroup.CHEST)
                .level(ExerciseLevel.INTERMEDIATE)
                .build();
    }

    @Test
    void search_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Exercise> page = new PageImpl<>(List.of(exercise));
        ExerciseResponse response = ExerciseResponse.builder().id(exerciseId).name("Bench Press").build();

        when(exerciseRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(exerciseMapper.toResponse(exercise)).thenReturn(response);

        Page<ExerciseResponse> result = exerciseService.search(ExerciseCategory.STRENGTH, MuscleGroup.CHEST,
                ExerciseLevel.INTERMEDIATE, "bench", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Bench Press");
    }

    @Test
    void findById_shouldReturnExercise_whenExists() {
        ExerciseResponse response = ExerciseResponse.builder().id(exerciseId).name("Bench Press").build();
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));
        when(exerciseMapper.toResponse(exercise)).thenReturn(response);

        ExerciseResponse result = exerciseService.findById(exerciseId);

        assertThat(result.getId()).isEqualTo(exerciseId);
        assertThat(result.getName()).isEqualTo("Bench Press");
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        UUID missingId = UUID.randomUUID();
        when(exerciseRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseService.findById(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Exercise");
    }

    @Test
    void create_shouldMapSaveAndReturnResponse() {
        ExerciseRequest request = new ExerciseRequest();
        request.setName("Squat");
        request.setCategory(ExerciseCategory.STRENGTH);
        request.setMuscleGroup(MuscleGroup.LEGS);

        Exercise entityToSave = Exercise.builder().name("Squat").category(ExerciseCategory.STRENGTH)
                .muscleGroup(MuscleGroup.LEGS).build();
        Exercise saved = Exercise.builder().id(UUID.randomUUID()).name("Squat").category(ExerciseCategory.STRENGTH)
                .muscleGroup(MuscleGroup.LEGS).build();
        ExerciseResponse response = ExerciseResponse.builder().id(saved.getId()).name("Squat").build();

        when(exerciseMapper.toEntity(request)).thenReturn(entityToSave);
        when(exerciseRepository.save(entityToSave)).thenReturn(saved);
        when(exerciseMapper.toResponse(saved)).thenReturn(response);

        ExerciseResponse result = exerciseService.create(request);

        assertThat(result.getName()).isEqualTo("Squat");
        verify(exerciseRepository, times(1)).save(entityToSave);
    }

    @Test
    void update_shouldThrow_whenNotFound() {
        UUID missingId = UUID.randomUUID();
        ExerciseRequest request = new ExerciseRequest();
        when(exerciseRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseService.update(missingId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateEntityAndSave_whenExists() {
        ExerciseRequest request = new ExerciseRequest();
        request.setName("Bench Press Updated");
        request.setCategory(ExerciseCategory.STRENGTH);
        request.setMuscleGroup(MuscleGroup.CHEST);

        ExerciseResponse response = ExerciseResponse.builder().id(exerciseId).name("Bench Press Updated").build();

        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));
        when(exerciseRepository.save(exercise)).thenReturn(exercise);
        when(exerciseMapper.toResponse(exercise)).thenReturn(response);

        ExerciseResponse result = exerciseService.update(exerciseId, request);

        assertThat(result.getName()).isEqualTo("Bench Press Updated");
        verify(exerciseMapper).updateEntity(request, exercise);
        verify(exerciseRepository).save(exercise);
    }

    @Test
    void delete_shouldRemoveExercise_whenExists() {
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));

        exerciseService.delete(exerciseId);

        verify(exerciseRepository, times(1)).delete(exercise);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        UUID missingId = UUID.randomUUID();
        when(exerciseRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseService.delete(missingId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(exerciseRepository, never()).delete(any(Exercise.class));
    }
}
