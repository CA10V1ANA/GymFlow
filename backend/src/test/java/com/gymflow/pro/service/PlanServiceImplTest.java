package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.PlanRequest;
import com.gymflow.pro.dto.response.PlanResponse;
import com.gymflow.pro.entity.Plan;
import com.gymflow.pro.entity.enums.PlanType;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.PlanMapper;
import com.gymflow.pro.repository.PlanRepository;
import com.gymflow.pro.service.impl.PlanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceImplTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private PlanMapper planMapper;

    private PlanServiceImpl planService;

    private Plan plan;
    private UUID planId;

    @BeforeEach
    void setUp() {
        planService = new PlanServiceImpl(planRepository, planMapper);
        planId = UUID.randomUUID();
        plan = Plan.builder()
                .id(planId)
                .name("Gold")
                .type(PlanType.MONTHLY)
                .durationMonths(1)
                .price(BigDecimal.valueOf(100))
                .discountPercentage(BigDecimal.ZERO)
                .active(true)
                .build();
    }

    private PlanRequest buildRequest() {
        PlanRequest request = new PlanRequest();
        request.setName("Gold");
        request.setType(PlanType.MONTHLY);
        request.setDurationMonths(1);
        request.setPrice(BigDecimal.valueOf(100));
        request.setDiscountPercentage(BigDecimal.ZERO);
        return request;
    }

    @Test
    void findAll_shouldUseFindByActive_whenActiveFilterProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Plan> page = new PageImpl<>(List.of(plan));
        PlanResponse response = PlanResponse.builder().id(planId).name("Gold").build();

        when(planRepository.findByActive(true, pageable)).thenReturn(page);
        when(planMapper.toResponse(plan)).thenReturn(response);

        Page<PlanResponse> result = planService.findAll(true, pageable);

        assertThat(result.getContent()).containsExactly(response);
        verify(planRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void findAll_shouldUseFindAll_whenActiveFilterNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Plan> page = new PageImpl<>(List.of(plan));
        PlanResponse response = PlanResponse.builder().id(planId).name("Gold").build();

        when(planRepository.findAll(pageable)).thenReturn(page);
        when(planMapper.toResponse(plan)).thenReturn(response);

        Page<PlanResponse> result = planService.findAll(null, pageable);

        assertThat(result.getContent()).containsExactly(response);
        verify(planRepository, never()).findByActive(anyBoolean(), any(Pageable.class));
    }

    @Test
    void findById_shouldReturnPlan_whenExists() {
        PlanResponse response = PlanResponse.builder().id(planId).name("Gold").build();
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planMapper.toResponse(plan)).thenReturn(response);

        PlanResponse result = planService.findById(planId);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void findById_shouldThrowResourceNotFound_whenMissing() {
        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planService.findById(planId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldDefaultActiveToTrue_whenNotProvided() {
        PlanRequest request = buildRequest();
        request.setActive(null);
        Plan mappedEntity = Plan.builder().name("Gold").type(PlanType.MONTHLY).durationMonths(1)
                .price(BigDecimal.valueOf(100)).build();
        mappedEntity.setActive(false);
        PlanResponse response = PlanResponse.builder().id(planId).name("Gold").build();

        when(planMapper.toEntity(request)).thenReturn(mappedEntity);
        when(planRepository.save(mappedEntity)).thenReturn(mappedEntity);
        when(planMapper.toResponse(mappedEntity)).thenReturn(response);

        PlanResponse result = planService.create(request);

        assertThat(result).isEqualTo(response);
        assertThat(mappedEntity.isActive()).isTrue();
        verify(planRepository).save(mappedEntity);
    }

    @Test
    void create_shouldRespectExplicitActiveFlag() {
        PlanRequest request = buildRequest();
        request.setActive(false);
        Plan mappedEntity = Plan.builder().name("Gold").type(PlanType.MONTHLY).durationMonths(1)
                .price(BigDecimal.valueOf(100)).active(false).build();
        PlanResponse response = PlanResponse.builder().id(planId).name("Gold").build();

        when(planMapper.toEntity(request)).thenReturn(mappedEntity);
        when(planRepository.save(mappedEntity)).thenReturn(mappedEntity);
        when(planMapper.toResponse(mappedEntity)).thenReturn(response);

        planService.create(request);

        assertThat(mappedEntity.isActive()).isFalse();
    }

    @Test
    void update_shouldThrowResourceNotFound_whenMissing() {
        PlanRequest request = buildRequest();
        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planService.update(planId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldUpdateEntityAndActiveFlag_whenProvided() {
        PlanRequest request = buildRequest();
        request.setActive(false);
        PlanResponse response = PlanResponse.builder().id(planId).name("Gold").build();

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planRepository.save(plan)).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(response);

        PlanResponse result = planService.update(planId, request);

        assertThat(result).isEqualTo(response);
        assertThat(plan.isActive()).isFalse();
        verify(planMapper).updateEntity(request, plan);
        verify(planRepository).save(plan);
    }

    @Test
    void update_shouldKeepExistingActive_whenActiveNotProvided() {
        PlanRequest request = buildRequest();
        request.setActive(null);
        plan.setActive(true);
        PlanResponse response = PlanResponse.builder().id(planId).build();

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planRepository.save(plan)).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(response);

        planService.update(planId, request);

        assertThat(plan.isActive()).isTrue();
    }

    @Test
    void delete_shouldSetActiveFalse_andSave() {
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planRepository.save(plan)).thenReturn(plan);

        planService.delete(planId);

        assertThat(plan.isActive()).isFalse();
        verify(planRepository).save(plan);
    }

    @Test
    void delete_shouldThrowResourceNotFound_whenMissing() {
        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planService.delete(planId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
