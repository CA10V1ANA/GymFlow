package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.request.PlanRequest;
import com.gymflow.pro.dto.response.PlanResponse;
import com.gymflow.pro.entity.Plan;
import com.gymflow.pro.entity.enums.PlanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PlanMapperTest {

    private PlanMapper planMapper;

    @BeforeEach
    void setUp() {
        planMapper = new PlanMapperImpl();
    }

    private PlanRequest buildRequest() {
        PlanRequest request = new PlanRequest();
        request.setName("Gold");
        request.setType(PlanType.MONTHLY);
        request.setDurationMonths(1);
        request.setPrice(BigDecimal.valueOf(100));
        return request;
    }

    @Test
    void toEntity_shouldMapAllFields_andDefaultActive_whenActiveNull() {
        Plan entity = planMapper.toEntity(buildRequest());

        assertThat(entity.getName()).isEqualTo("Gold");
        assertThat(entity.getType()).isEqualTo(PlanType.MONTHLY);
        assertThat(entity.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void toEntity_shouldSetActiveExplicitly_whenProvided() {
        PlanRequest request = buildRequest();
        request.setActive(false);

        Plan entity = planMapper.toEntity(request);

        assertThat(entity.isActive()).isFalse();
    }

    @Test
    void toEntity_shouldReturnNull_whenRequestNull() {
        assertThat(planMapper.toEntity(null)).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        Plan plan = Plan.builder().id(id).name("Gold").type(PlanType.MONTHLY)
                .durationMonths(1).price(BigDecimal.valueOf(100)).active(true).build();

        PlanResponse response = planMapper.toResponse(plan);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Gold");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void toResponse_shouldReturnNull_whenPlanNull() {
        assertThat(planMapper.toResponse(null)).isNull();
    }

    @Test
    void updateEntity_shouldOnlyOverwriteNonNullFields() {
        Plan plan = Plan.builder().name("Old").description("Old desc").build();
        PlanRequest request = new PlanRequest();
        request.setName("New");

        planMapper.updateEntity(request, plan);

        assertThat(plan.getName()).isEqualTo("New");
        assertThat(plan.getDescription()).isEqualTo("Old desc");
    }

    @Test
    void updateEntity_shouldDoNothing_whenRequestNull() {
        Plan plan = Plan.builder().name("Old").build();

        planMapper.updateEntity(null, plan);

        assertThat(plan.getName()).isEqualTo("Old");
    }
}
