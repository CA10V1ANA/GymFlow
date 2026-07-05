package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.StudentRequest;
import com.gymflow.pro.dto.request.StudentSelfUpdateRequest;
import com.gymflow.pro.dto.response.EnrollmentResponse;
import com.gymflow.pro.dto.response.StudentResponse;
import com.gymflow.pro.entity.enums.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface StudentService {

    Page<StudentResponse> search(String search, StudentStatus status, Pageable pageable);

    StudentResponse findById(UUID id);

    StudentResponse create(StudentRequest request);

    StudentResponse update(UUID id, StudentRequest request);

    StudentResponse updateSelf(UUID id, StudentSelfUpdateRequest request);

    void delete(UUID id);

    List<EnrollmentResponse> enrollmentHistory(UUID studentId);
}
