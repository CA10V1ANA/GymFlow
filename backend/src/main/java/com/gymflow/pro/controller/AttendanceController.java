package com.gymflow.pro.controller;

import com.gymflow.pro.dto.request.CheckInRequest;
import com.gymflow.pro.dto.response.AttendanceResponse;
import com.gymflow.pro.service.AttendanceService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
@Tag(name = "Attendances", description = "Student check-in / check-out tracking")
public class AttendanceController {

    private final AttendanceService attendanceService;

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
    public ResponseEntity<List<AttendanceResponse>> historyByStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(attendanceService.historyByStudent(studentId));
    }

    @GetMapping("/student/{studentId}/frequency")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','INSTRUCTOR','STUDENT')")
    public ResponseEntity<Map<String, Long>> frequency(@PathVariable UUID studentId) {
        return ResponseEntity.ok(Map.of(
                "daily", attendanceService.dailyFrequency(studentId),
                "monthly", attendanceService.monthlyFrequency(studentId)));
    }
}
