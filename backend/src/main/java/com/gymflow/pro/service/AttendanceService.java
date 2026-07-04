package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.CheckInRequest;
import com.gymflow.pro.dto.response.AttendanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AttendanceService {

    AttendanceResponse checkIn(CheckInRequest request);

    AttendanceResponse checkOut(String registrationCode);

    AttendanceResponse checkOutByAttendanceId(UUID attendanceId);

    Page<AttendanceResponse> findAll(UUID studentId, Pageable pageable);

    List<AttendanceResponse> historyByStudent(UUID studentId);

    long monthlyFrequency(UUID studentId);

    long dailyFrequency(UUID studentId);
}
