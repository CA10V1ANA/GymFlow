package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.WorkoutExerciseRequest;
import com.gymflow.pro.dto.request.WorkoutRequest;
import com.gymflow.pro.dto.response.WorkoutResponse;
import com.gymflow.pro.entity.Exercise;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.User;
import com.gymflow.pro.entity.Workout;
import com.gymflow.pro.entity.WorkoutExercise;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.WorkoutMapper;
import com.gymflow.pro.repository.ExerciseRepository;
import com.gymflow.pro.repository.StudentRepository;
import com.gymflow.pro.repository.UserRepository;
import com.gymflow.pro.repository.WorkoutRepository;
import com.gymflow.pro.service.impl.WorkoutServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceImplTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private WorkoutMapper workoutMapper;

    @InjectMocks
    private WorkoutServiceImpl workoutService;

    private UUID studentId;
    private UUID workoutId;
    private UUID exerciseId;
    private Student student;
    private Workout workout;
    private Exercise exercise;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        workoutId = UUID.randomUUID();
        exerciseId = UUID.randomUUID();

        student = Student.builder().id(studentId).name("John Doe").build();
        exercise = Exercise.builder().id(exerciseId).name("Squat").build();
        workout = Workout.builder().id(workoutId).student(student).name("Plan A").build();
    }

    @Test
    void findByStudent_shouldReturnMappedPage_whenStudentExists() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Workout> page = new PageImpl<>(List.of(workout));
        WorkoutResponse response = WorkoutResponse.builder().id(workoutId).name("Plan A").build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(workoutRepository.findByStudentOrderByCreatedAtDesc(student, pageable)).thenReturn(page);
        when(workoutMapper.toResponse(workout)).thenReturn(response);

        Page<WorkoutResponse> result = workoutService.findByStudent(studentId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Plan A");
    }

    @Test
    void findByStudent_shouldThrow_whenStudentNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.findByStudent(studentId, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student");
    }

    @Test
    void findById_shouldReturnWorkout_whenExists() {
        WorkoutResponse response = WorkoutResponse.builder().id(workoutId).name("Plan A").build();
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(workoutMapper.toResponse(workout)).thenReturn(response);

        WorkoutResponse result = workoutService.findById(workoutId);

        assertThat(result.getId()).isEqualTo(workoutId);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        UUID missingId = UUID.randomUUID();
        when(workoutRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.findById(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workout");
    }

    @Test
    void create_shouldBuildWorkoutWithExercisesAndSave_withoutInstructor() {
        WorkoutExerciseRequest exerciseRequest = new WorkoutExerciseRequest();
        exerciseRequest.setExerciseId(exerciseId);
        exerciseRequest.setSets(3);
        exerciseRequest.setRepetitions("10");

        WorkoutRequest request = new WorkoutRequest();
        request.setStudentId(studentId);
        request.setName("Plan A");
        request.setExercises(List.of(exerciseRequest));

        WorkoutResponse response = WorkoutResponse.builder().id(workoutId).name("Plan A").build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));
        when(workoutRepository.save(any(Workout.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workoutMapper.toResponse(any(Workout.class))).thenReturn(response);

        WorkoutResponse result = workoutService.create(request);

        assertThat(result.getName()).isEqualTo("Plan A");
        verify(userRepository, never()).findById(any());

        ArgumentCaptor<Workout> captor = ArgumentCaptor.forClass(Workout.class);
        verify(workoutRepository).save(captor.capture());
        Workout saved = captor.getValue();
        assertThat(saved.getStudent()).isEqualTo(student);
        assertThat(saved.getInstructor()).isNull();
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getStartDate()).isEqualTo(LocalDate.now());
        assertThat(saved.getExercises()).hasSize(1);
        assertThat(saved.getExercises().get(0).getExercise()).isEqualTo(exercise);
        assertThat(saved.getExercises().get(0).getSets()).isEqualTo(3);
        assertThat(saved.getExercises().get(0).getSortOrder()).isEqualTo(0);
    }

    @Test
    void create_shouldAttachInstructor_whenInstructorIdProvided() {
        UUID instructorId = UUID.randomUUID();
        User instructor = User.builder().id(instructorId).build();

        WorkoutExerciseRequest exerciseRequest = new WorkoutExerciseRequest();
        exerciseRequest.setExerciseId(exerciseId);
        exerciseRequest.setSets(3);
        exerciseRequest.setRepetitions("10");
        exerciseRequest.setSortOrder(5);
        exerciseRequest.setLoadKg(BigDecimal.TEN);

        WorkoutRequest request = new WorkoutRequest();
        request.setStudentId(studentId);
        request.setInstructorId(instructorId);
        request.setName("Plan B");
        request.setActive(false);
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setExercises(List.of(exerciseRequest));

        WorkoutResponse response = WorkoutResponse.builder().id(workoutId).name("Plan B").build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(userRepository.findById(instructorId)).thenReturn(Optional.of(instructor));
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));
        when(workoutRepository.save(any(Workout.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workoutMapper.toResponse(any(Workout.class))).thenReturn(response);

        workoutService.create(request);

        ArgumentCaptor<Workout> captor = ArgumentCaptor.forClass(Workout.class);
        verify(workoutRepository).save(captor.capture());
        Workout saved = captor.getValue();
        assertThat(saved.getInstructor()).isEqualTo(instructor);
        assertThat(saved.isActive()).isFalse();
        assertThat(saved.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(saved.getExercises().get(0).getSortOrder()).isEqualTo(5);
        assertThat(saved.getExercises().get(0).getLoadKg()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void create_shouldThrow_whenStudentNotFound() {
        WorkoutRequest request = new WorkoutRequest();
        request.setStudentId(studentId);
        request.setName("Plan A");

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student");

        verify(workoutRepository, never()).save(any());
    }

    @Test
    void create_shouldThrow_whenInstructorNotFound() {
        UUID instructorId = UUID.randomUUID();
        WorkoutRequest request = new WorkoutRequest();
        request.setStudentId(studentId);
        request.setInstructorId(instructorId);
        request.setName("Plan A");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(userRepository.findById(instructorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Instructor");

        verify(workoutRepository, never()).save(any());
    }

    @Test
    void create_shouldThrow_whenExerciseNotFound() {
        WorkoutExerciseRequest exerciseRequest = new WorkoutExerciseRequest();
        exerciseRequest.setExerciseId(exerciseId);
        exerciseRequest.setSets(3);
        exerciseRequest.setRepetitions("10");

        WorkoutRequest request = new WorkoutRequest();
        request.setStudentId(studentId);
        request.setName("Plan A");
        request.setExercises(List.of(exerciseRequest));

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Exercise");

        verify(workoutRepository, never()).save(any());
    }

    @Test
    void update_shouldReplaceExercisesAndFields_whenWorkoutExists() {
        WorkoutExercise oldExercise = WorkoutExercise.builder().workout(workout).exercise(exercise).sets(1)
                .repetitions("5").build();
        workout.getExercises().add(oldExercise);

        WorkoutExerciseRequest newExerciseRequest = new WorkoutExerciseRequest();
        newExerciseRequest.setExerciseId(exerciseId);
        newExerciseRequest.setSets(4);
        newExerciseRequest.setRepetitions("12");

        WorkoutRequest request = new WorkoutRequest();
        request.setStudentId(studentId);
        request.setName("Plan A Updated");
        request.setEndDate(LocalDate.of(2026, 12, 31));
        request.setExercises(List.of(newExerciseRequest));

        WorkoutResponse response = WorkoutResponse.builder().id(workoutId).name("Plan A Updated").build();

        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));
        when(workoutRepository.save(workout)).thenReturn(workout);
        when(workoutMapper.toResponse(workout)).thenReturn(response);

        WorkoutResponse result = workoutService.update(workoutId, request);

        assertThat(result.getName()).isEqualTo("Plan A Updated");
        assertThat(workout.getExercises()).hasSize(1);
        assertThat(workout.getExercises().get(0).getSets()).isEqualTo(4);
        assertThat(workout.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        verify(workoutRepository).save(workout);
    }

    @Test
    void update_shouldThrow_whenWorkoutNotFound() {
        UUID missingId = UUID.randomUUID();
        WorkoutRequest request = new WorkoutRequest();
        when(workoutRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.update(missingId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(workoutRepository, never()).save(any());
    }

    @Test
    void delete_shouldRemoveWorkout_whenExists() {
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));

        workoutService.delete(workoutId);

        verify(workoutRepository, times(1)).delete(workout);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        UUID missingId = UUID.randomUUID();
        when(workoutRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.delete(missingId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(workoutRepository, never()).delete(any());
    }
}
