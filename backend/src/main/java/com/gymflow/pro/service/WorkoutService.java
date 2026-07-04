package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.WorkoutRequest;
import com.gymflow.pro.dto.response.WorkoutResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WorkoutService {

    Page<WorkoutResponse> findAll(Pageable pageable);

    Page<WorkoutResponse> findByStudent(UUID studentId, Pageable pageable);

    WorkoutResponse findById(UUID id);

    WorkoutResponse create(WorkoutRequest request);

    WorkoutResponse update(UUID id, WorkoutRequest request);

    void delete(UUID id);
}
