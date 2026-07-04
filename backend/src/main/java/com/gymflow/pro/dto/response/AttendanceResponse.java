package com.gymflow.pro.dto.response;

import com.gymflow.pro.entity.enums.AttendanceMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private UUID id;
    private StudentSummaryResponse student;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private AttendanceMethod method;
    private Long permanenceMinutes;
}
