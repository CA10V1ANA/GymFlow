package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.EnrollmentResponse;
import com.gymflow.pro.entity.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {StudentMapper.class, PlanMapper.class})
public interface EnrollmentMapper {

    @Mapping(target = "student", source = "student")
    @Mapping(target = "plan", source = "plan")
    EnrollmentResponse toResponse(Enrollment enrollment);
}
