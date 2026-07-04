package com.gymflow.pro.service.impl;

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
import com.gymflow.pro.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutServiceImpl implements WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutMapper workoutMapper;

    @Override
    public Page<WorkoutResponse> findAll(Pageable pageable) {
        return workoutRepository.findAllByOrderByCreatedAtDesc(pageable).map(workoutMapper::toResponse);
    }

    @Override
    public Page<WorkoutResponse> findByStudent(UUID studentId, Pageable pageable) {
        Student student = getStudentOrThrow(studentId);
        return workoutRepository.findByStudentOrderByCreatedAtDesc(student, pageable).map(workoutMapper::toResponse);
    }

    @Override
    public WorkoutResponse findById(UUID id) {
        return workoutMapper.toResponse(getWorkoutOrThrow(id));
    }

    @Override
    @Transactional
    public WorkoutResponse create(WorkoutRequest request) {
        Student student = getStudentOrThrow(request.getStudentId());
        User instructor = request.getInstructorId() != null
                ? userRepository.findById(request.getInstructorId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Instructor", request.getInstructorId()))
                : null;

        Workout workout = Workout.builder()
                .student(student)
                .instructor(instructor)
                .name(request.getName())
                .goal(request.getGoal())
                .active(request.getActive() == null || request.getActive())
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .endDate(request.getEndDate())
                .notes(request.getNotes())
                .build();

        attachExercises(workout, request.getExercises());

        return workoutMapper.toResponse(workoutRepository.save(workout));
    }

    @Override
    @Transactional
    public WorkoutResponse update(UUID id, WorkoutRequest request) {
        Workout workout = getWorkoutOrThrow(id);

        if (request.getInstructorId() != null) {
            User instructor = userRepository.findById(request.getInstructorId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Instructor", request.getInstructorId()));
            workout.setInstructor(instructor);
        }

        workout.setName(request.getName());
        workout.setGoal(request.getGoal());
        if (request.getActive() != null) {
            workout.setActive(request.getActive());
        }
        if (request.getStartDate() != null) {
            workout.setStartDate(request.getStartDate());
        }
        workout.setEndDate(request.getEndDate());
        workout.setNotes(request.getNotes());

        workout.getExercises().clear();
        attachExercises(workout, request.getExercises());

        return workoutMapper.toResponse(workoutRepository.save(workout));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        workoutRepository.delete(getWorkoutOrThrow(id));
    }

    private void attachExercises(Workout workout, List<WorkoutExerciseRequest> requests) {
        if (requests == null) {
            return;
        }
        List<WorkoutExercise> exercises = new ArrayList<>();
        int order = 0;
        for (WorkoutExerciseRequest req : requests) {
            Exercise exercise = exerciseRepository.findById(req.getExerciseId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Exercise", req.getExerciseId()));
            exercises.add(WorkoutExercise.builder()
                    .workout(workout)
                    .exercise(exercise)
                    .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : order++)
                    .sets(req.getSets())
                    .repetitions(req.getRepetitions())
                    .loadKg(req.getLoadKg())
                    .durationSeconds(req.getDurationSeconds())
                    .restSeconds(req.getRestSeconds())
                    .notes(req.getNotes())
                    .build());
        }
        workout.getExercises().addAll(exercises);
    }

    private Workout getWorkoutOrThrow(UUID id) {
        return workoutRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Workout", id));
    }

    private Student getStudentOrThrow(UUID id) {
        return studentRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Student", id));
    }
}
