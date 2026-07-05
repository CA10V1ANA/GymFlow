package com.gymflow.pro.controller;

import com.gymflow.pro.dto.request.StudentRequest;
import com.gymflow.pro.dto.request.StudentSelfUpdateRequest;
import com.gymflow.pro.dto.response.EnrollmentResponse;
import com.gymflow.pro.dto.response.StudentResponse;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.StudentStatus;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.security.SecurityUtils;
import com.gymflow.pro.service.StudentService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Student registration and management")
public class StudentController {

    private final StudentService studentService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','INSTRUCTOR')")
    public ResponseEntity<Page<StudentResponse>> search(@RequestParam(required = false) String search,
                                                          @RequestParam(required = false) StudentStatus status,
                                                          Pageable pageable) {
        return ResponseEntity.ok(studentService.search(search, status, pageable));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentResponse> me(Authentication authentication) {
        return ResponseEntity.ok(studentService.findById(currentStudentIdOrThrow(authentication)));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentResponse> updateMe(@Valid @RequestBody StudentSelfUpdateRequest request,
                                                    Authentication authentication) {
        return ResponseEntity.ok(studentService.updateSelf(currentStudentIdOrThrow(authentication), request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','INSTRUCTOR','STUDENT')")
    public ResponseEntity<StudentResponse> findById(@PathVariable UUID id, Authentication authentication) {
        securityUtils.assertOwnStudentIfStudentRole(id, authentication);
        return ResponseEntity.ok(studentService.findById(id));
    }

    @GetMapping("/{id}/enrollments")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','STUDENT')")
    public ResponseEntity<List<EnrollmentResponse>> enrollmentHistory(@PathVariable UUID id, Authentication authentication) {
        securityUtils.assertOwnStudentIfStudentRole(id, authentication);
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

    private UUID currentStudentIdOrThrow(Authentication authentication) {
        return securityUtils.currentStudent(authentication)
                .map(Student::getId)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile linked to the current account"));
    }
}
