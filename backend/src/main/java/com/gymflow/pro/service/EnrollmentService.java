package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.EnrollmentRequest;
import com.gymflow.pro.dto.response.EnrollmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EnrollmentService {

    Page<EnrollmentResponse> findAll(Pageable pageable);

    EnrollmentResponse findById(UUID id);

    EnrollmentResponse create(EnrollmentRequest request);

    EnrollmentResponse renew(UUID id, EnrollmentRequest request);

    EnrollmentResponse cancel(UUID id, String reason);

    EnrollmentResponse freeze(UUID id);

    EnrollmentResponse reactivate(UUID id);
}
