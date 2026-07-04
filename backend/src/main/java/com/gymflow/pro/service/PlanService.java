package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.PlanRequest;
import com.gymflow.pro.dto.response.PlanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PlanService {

    Page<PlanResponse> findAll(Boolean active, Pageable pageable);

    PlanResponse findById(UUID id);

    PlanResponse create(PlanRequest request);

    PlanResponse update(UUID id, PlanRequest request);

    void delete(UUID id);
}
