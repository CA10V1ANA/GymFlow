package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.AttendanceResponse;
import com.gymflow.pro.entity.Attendance;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.AttendanceMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AttendanceMapperTest {

    private AttendanceMapper attendanceMapper;

    @BeforeEach
    void setUp() {
        AttendanceMapperImpl impl = new AttendanceMapperImpl();
        ReflectionTestUtils.setField(impl, "studentMapper", new StudentMapperImpl());
        attendanceMapper = impl;
    }

    @Test
    void toResponse_shouldComputePermanenceMinutes_whenCheckOutPresent() {
        UUID id = UUID.randomUUID();
        LocalDateTime checkIn = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime checkOut = LocalDateTime.of(2026, 1, 1, 11, 30);
        Attendance attendance = Attendance.builder()
                .id(id)
                .student(Student.builder().name("Jane").build())
                .checkIn(checkIn)
                .checkOut(checkOut)
                .method(AttendanceMethod.CODE)
                .build();

        AttendanceResponse response = attendanceMapper.toResponse(attendance);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getStudent().getName()).isEqualTo("Jane");
        assertThat(response.getPermanenceMinutes()).isEqualTo(90L);
    }

    @Test
    void toResponse_shouldReturnNullPermanenceMinutes_whenCheckOutAbsent() {
        Attendance attendance = Attendance.builder()
                .student(Student.builder().name("Jane").build())
                .checkIn(LocalDateTime.now())
                .checkOut(null)
                .method(AttendanceMethod.CODE)
                .build();

        AttendanceResponse response = attendanceMapper.toResponse(attendance);

        assertThat(response.getPermanenceMinutes()).isNull();
    }

    @Test
    void toResponse_shouldReturnNull_whenAttendanceNull() {
        assertThat(attendanceMapper.toResponse(null)).isNull();
    }
}
