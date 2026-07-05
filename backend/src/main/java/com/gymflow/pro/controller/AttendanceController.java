package com.gymflow.pro.controller;

import com.gymflow.pro.dto.request.CheckInRequest;
import com.gymflow.pro.dto.response.AttendanceResponse;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.security.SecurityUtils;
import com.gymflow.pro.service.AttendanceService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
@Tag(name = "Attendances", description = "Student check-in / check-out tracking")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final SecurityUtils securityUtils;

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<AttendanceResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.checkIn(request));
    }

    @PostMapping("/check-out/{registrationCode}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<AttendanceResponse> checkOut(@PathVariable String registrationCode) {
        return ResponseEntity.ok(attendanceService.checkOut(registrationCode));
    }

    @PostMapping("/{attendanceId}/check-out")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<AttendanceResponse> checkOutByAttendanceId(@PathVariable UUID attendanceId) {
        return ResponseEntity.ok(attendanceService.checkOutByAttendanceId(attendanceId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','INSTRUCTOR')")
    public ResponseEntity<Page<AttendanceResponse>> findAll(@RequestParam(required = false) UUID studentId, Pageable pageable) {
        return ResponseEntity.ok(attendanceService.findAll(studentId, pageable));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','INSTRUCTOR','STUDENT')")
    public ResponseEntity<List<AttendanceResponse>> historyByStudent(@PathVariable UUID studentId, Authentication authentication) {
        securityUtils.assertOwnStudentIfStudentRole(studentId, authentication);
        return ResponseEntity.ok(attendanceService.historyByStudent(studentId));
    }

    @GetMapping("/student/{studentId}/frequency")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','INSTRUCTOR','STUDENT')")
    public ResponseEntity<Map<String, Long>> frequency(@PathVariable UUID studentId, Authentication authentication) {
        securityUtils.assertOwnStudentIfStudentRole(studentId, authentication);
        return ResponseEntity.ok(Map.of(
                "daily", attendanceService.dailyFrequency(studentId),
                "monthly", attendanceService.monthlyFrequency(studentId)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AttendanceResponse>> myHistory(Authentication authentication) {
        return ResponseEntity.ok(attendanceService.historyByStudent(currentStudentIdOrThrow(authentication)));
    }

    @GetMapping("/me/frequency")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Long>> myFrequency(Authentication authentication) {
        UUID studentId = currentStudentIdOrThrow(authentication);
        return ResponseEntity.ok(Map.of(
                "daily", attendanceService.dailyFrequency(studentId),
                "monthly", attendanceService.monthlyFrequency(studentId)));
    }

    private UUID currentStudentIdOrThrow(Authentication authentication) {
        return securityUtils.currentStudent(authentication)
                .map(Student::getId)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile linked to the current account"));
    }
}
