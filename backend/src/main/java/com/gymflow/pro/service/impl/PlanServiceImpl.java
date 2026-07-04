package com.gymflow.pro.service.impl;

import com.gymflow.pro.dto.request.PlanRequest;
import com.gymflow.pro.dto.response.PlanResponse;
import com.gymflow.pro.entity.Plan;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.PlanMapper;
import com.gymflow.pro.repository.PlanRepository;
import com.gymflow.pro.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;

    @Override
    public Page<PlanResponse> findAll(Boolean active, Pageable pageable) {
        Page<Plan> page = active != null
                ? planRepository.findByActive(active, pageable)
                : planRepository.findAll(pageable);
        return page.map(planMapper::toResponse);
    }

    @Override
    public PlanResponse findById(UUID id) {
        return planMapper.toResponse(getPlanOrThrow(id));
    }

    @Override
    @Transactional
    public PlanResponse create(PlanRequest request) {
        Plan plan = planMapper.toEntity(request);
        if (request.getActive() == null) {
            plan.setActive(true);
        }
        return planMapper.toResponse(planRepository.save(plan));
    }

    @Override
    @Transactional
    public PlanResponse update(UUID id, PlanRequest request) {
        Plan plan = getPlanOrThrow(id);
        planMapper.updateEntity(request, plan);
        if (request.getActive() != null) {
            plan.setActive(request.getActive());
        }
        return planMapper.toResponse(planRepository.save(plan));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Plan plan = getPlanOrThrow(id);
        plan.setActive(false);
        planRepository.save(plan);
    }

    private Plan getPlanOrThrow(UUID id) {
        return planRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Plan", id));
    }
}
