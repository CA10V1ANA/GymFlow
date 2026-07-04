package com.gymflow.pro.controller;

import com.gymflow.pro.dto.request.ExerciseRequest;
import com.gymflow.pro.dto.response.ExerciseResponse;
import com.gymflow.pro.entity.enums.ExerciseCategory;
import com.gymflow.pro.entity.enums.ExerciseLevel;
import com.gymflow.pro.entity.enums.MuscleGroup;
import com.gymflow.pro.service.ExerciseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@Tag(name = "Exercises", description = "Exercise library")
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping
    public ResponseEntity<Page<ExerciseResponse>> search(@RequestParam(required = false) ExerciseCategory category,
                                                           @RequestParam(required = false) MuscleGroup muscleGroup,
                                                           @RequestParam(required = false) ExerciseLevel level,
                                                           @RequestParam(required = false) String search,
                                                           Pageable pageable) {
        return ResponseEntity.ok(exerciseService.search(category, muscleGroup, level, search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(exerciseService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ExerciseResponse> create(@Valid @RequestBody ExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ExerciseResponse> update(@PathVariable UUID id, @Valid @RequestBody ExerciseRequest request) {
        return ResponseEntity.ok(exerciseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        exerciseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
