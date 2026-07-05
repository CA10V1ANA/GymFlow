package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.request.StudentRequest;
import com.gymflow.pro.dto.request.StudentSelfUpdateRequest;
import com.gymflow.pro.dto.response.StudentResponse;
import com.gymflow.pro.dto.response.StudentSummaryResponse;
import com.gymflow.pro.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StudentMapper {

    Student toEntity(StudentRequest request);

    StudentResponse toResponse(Student student);

    StudentSummaryResponse toSummary(Student student);

    void updateEntity(StudentRequest request, @MappingTarget Student student);

    void updateSelfEntity(StudentSelfUpdateRequest request, @MappingTarget Student student);
}
