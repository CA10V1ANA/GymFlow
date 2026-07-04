package com.gymflow.pro.controller;

import com.gymflow.pro.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/**
 * Aggregated report data. Export to PDF/Excel is intentionally out of scope for this phase;
 * these endpoints return flat, export-friendly JSON so a future export layer (e.g. using
 * Apache POI for Excel or OpenPDF for PDF) can consume it directly.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Aggregated report data for students, financial, attendance, plans and products")
@PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/students")
    public ResponseEntity<Map<String, Object>> students() {
        return ResponseEntity.ok(reportService.studentsReport());
    }

    @GetMapping("/financial")
    public ResponseEntity<Map<String, Object>> financial(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.financialReport(startDate, endDate));
    }

    @GetMapping("/attendance")
    public ResponseEntity<Map<String, Object>> attendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.attendanceReport(startDate, endDate));
    }

    @GetMapping("/plans")
    public ResponseEntity<Map<String, Object>> plans() {
        return ResponseEntity.ok(reportService.plansReport());
    }

    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> products() {
        return ResponseEntity.ok(reportService.productsReport());
    }
}
