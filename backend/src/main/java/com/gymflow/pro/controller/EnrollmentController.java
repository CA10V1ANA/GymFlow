package com.gymflow.pro.controller;

import com.gymflow.pro.dto.request.CancelEnrollmentRequest;
import com.gymflow.pro.dto.request.EnrollmentRequest;
import com.gymflow.pro.dto.response.EnrollmentResponse;
import com.gymflow.pro.service.EnrollmentService;
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
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Student membership enrollment lifecycle")
@PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    public ResponseEntity<Page<EnrollmentResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(enrollmentService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(enrollmentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> create(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.create(request));
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<EnrollmentResponse> renew(@PathVariable UUID id, @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.renew(id, request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<EnrollmentResponse> cancel(@PathVariable UUID id, @Valid @RequestBody CancelEnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.cancel(id, request.getReason()));
    }

    @PostMapping("/{id}/freeze")
    public ResponseEntity<EnrollmentResponse> freeze(@PathVariable UUID id) {
        return ResponseEntity.ok(enrollmentService.freeze(id));
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<EnrollmentResponse> reactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(enrollmentService.reactivate(id));
    }
}
