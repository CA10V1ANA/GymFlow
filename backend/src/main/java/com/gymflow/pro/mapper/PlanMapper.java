package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.request.PlanRequest;
import com.gymflow.pro.dto.response.PlanResponse;
import com.gymflow.pro.entity.Plan;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PlanMapper {

    Plan toEntity(PlanRequest request);

    PlanResponse toResponse(Plan plan);

    void updateEntity(PlanRequest request, @MappingTarget Plan plan);
}
