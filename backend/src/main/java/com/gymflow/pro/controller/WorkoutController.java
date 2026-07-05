package com.gymflow.pro.controller;

import com.gymflow.pro.dto.request.WorkoutRequest;
import com.gymflow.pro.dto.response.WorkoutResponse;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.security.SecurityUtils;
import com.gymflow.pro.service.WorkoutService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
@Tag(name = "Workouts", description = "Student workout plans")
public class WorkoutController {

    private final WorkoutService workoutService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR','RECEPTIONIST')")
    public ResponseEntity<Page<WorkoutResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(workoutService.findAll(pageable));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR','RECEPTIONIST','STUDENT')")
    public ResponseEntity<Page<WorkoutResponse>> findByStudent(@PathVariable UUID studentId, Pageable pageable,
                                                               Authentication authentication) {
        securityUtils.assertOwnStudentIfStudentRole(studentId, authentication);
        return ResponseEntity.ok(workoutService.findByStudent(studentId, pageable));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Page<WorkoutResponse>> myWorkouts(Pageable pageable, Authentication authentication) {
        return ResponseEntity.ok(workoutService.findByStudent(currentStudentIdOrThrow(authentication), pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR','RECEPTIONIST','STUDENT')")
    public ResponseEntity<WorkoutResponse> findById(@PathVariable UUID id, Authentication authentication) {
        securityUtils.assertOwnWorkoutIfStudentRole(id, authentication);
        return ResponseEntity.ok(workoutService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<WorkoutResponse> create(@Valid @RequestBody WorkoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<WorkoutResponse> update(@PathVariable UUID id, @Valid @RequestBody WorkoutRequest request) {
        return ResponseEntity.ok(workoutService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        workoutService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UUID currentStudentIdOrThrow(Authentication authentication) {
        return securityUtils.currentStudent(authentication)
                .map(Student::getId)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile linked to the current account"));
    }
}
