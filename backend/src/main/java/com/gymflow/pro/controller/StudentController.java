package com.gymflow.pro.controller;

import com.gymflow.pro.dto.request.StudentRequest;
import com.gymflow.pro.dto.response.EnrollmentResponse;
import com.gymflow.pro.dto.response.StudentResponse;
import com.gymflow.pro.entity.enums.StudentStatus;
import com.gymflow.pro.service.StudentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Student registration and management")
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','INSTRUCTOR')")
    public ResponseEntity<Page<StudentResponse>> search(@RequestParam(required = false) String search,
                                                          @RequestParam(required = false) StudentStatus status,
                                                          Pageable pageable) {
        return ResponseEntity.ok(studentService.search(search, status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','INSTRUCTOR','STUDENT')")
    public ResponseEntity<StudentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(studentService.findById(id));
    }

    @GetMapping("/{id}/enrollments")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','STUDENT')")
    public ResponseEntity<List<EnrollmentResponse>> enrollmentHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(studentService.enrollmentHistory(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<StudentResponse> update(@PathVariable UUID id, @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
