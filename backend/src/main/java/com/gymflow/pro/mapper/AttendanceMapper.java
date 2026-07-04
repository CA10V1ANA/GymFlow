package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.AttendanceResponse;
import com.gymflow.pro.entity.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;

@Mapper(componentModel = "spring", uses = StudentMapper.class)
public abstract class AttendanceMapper {

    @Mapping(target = "student", source = "student")
    @Mapping(target = "permanenceMinutes", expression = "java(permanenceMinutes(attendance))")
    public abstract AttendanceResponse toResponse(Attendance attendance);

    protected Long permanenceMinutes(Attendance attendance) {
        Duration duration = attendance.getPermanenceDuration();
        return duration == null ? null : duration.toMinutes();
    }
}
