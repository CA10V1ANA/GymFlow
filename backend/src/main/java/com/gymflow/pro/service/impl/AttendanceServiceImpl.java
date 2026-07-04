package com.gymflow.pro.service.impl;

import com.gymflow.pro.dto.request.CheckInRequest;
import com.gymflow.pro.dto.response.AttendanceResponse;
import com.gymflow.pro.entity.Attendance;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.AttendanceMethod;
import com.gymflow.pro.exception.BusinessException;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.AttendanceMapper;
import com.gymflow.pro.repository.AttendanceRepository;
import com.gymflow.pro.repository.StudentRepository;
import com.gymflow.pro.repository.specification.AttendanceSpecifications;
import com.gymflow.pro.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final AttendanceMapper attendanceMapper;

    @Override
    @Transactional
    public AttendanceResponse checkIn(CheckInRequest request) {
        Student student = studentRepository.findByRegistrationCode(request.getRegistrationCode())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with registration code: " + request.getRegistrationCode()));

        attendanceRepository.findFirstByStudentAndCheckOutIsNullOrderByCheckInDesc(student)
                .ifPresent(open -> {
                    throw new BusinessException("Student already has an open check-in. Check out first.");
                });

        Attendance attendance = Attendance.builder()
                .student(student)
                .checkIn(LocalDateTime.now())
                .method(request.getMethod() != null ? request.getMethod() : AttendanceMethod.CODE)
                .build();

        return attendanceMapper.toResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(String registrationCode) {
        Student student = studentRepository.findByRegistrationCode(registrationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with registration code: " + registrationCode));

        Attendance attendance = attendanceRepository.findFirstByStudentAndCheckOutIsNullOrderByCheckInDesc(student)
                .orElseThrow(() -> new BusinessException("No open check-in found for this student"));

        attendance.setCheckOut(LocalDateTime.now());
        return attendanceMapper.toResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional
    public AttendanceResponse checkOutByAttendanceId(UUID attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> ResourceNotFoundException.of("Attendance", attendanceId));

        if (attendance.getCheckOut() != null) {
            throw new BusinessException("Attendance is already closed");
        }

        attendance.setCheckOut(LocalDateTime.now());
        return attendanceMapper.toResponse(attendanceRepository.save(attendance));
    }

    @Override
    public Page<AttendanceResponse> findAll(UUID studentId, Pageable pageable) {
        return attendanceRepository.findAll(AttendanceSpecifications.withStudent(studentId), pageable)
                .map(attendanceMapper::toResponse);
    }

    @Override
    public List<AttendanceResponse> historyByStudent(UUID studentId) {
        Student student = getStudentOrThrow(studentId);
        return attendanceRepository.findByStudentOrderByCheckInDesc(student).stream()
                .map(attendanceMapper::toResponse)
                .toList();
    }

    @Override
    public long monthlyFrequency(UUID studentId) {
        Student student = getStudentOrThrow(studentId);
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);
        return attendanceRepository.countByStudentAndCheckInBetween(student, start, end);
    }

    @Override
    public long dailyFrequency(UUID studentId) {
        Student student = getStudentOrThrow(studentId);
        LocalDate today = LocalDate.now();
        return attendanceRepository.countByStudentAndCheckInBetween(student, today.atStartOfDay(), today.atTime(LocalTime.MAX));
    }

    private Student getStudentOrThrow(UUID id) {
        return studentRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Student", id));
    }
}
