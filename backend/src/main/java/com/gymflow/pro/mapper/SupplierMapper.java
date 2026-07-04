package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.request.SupplierRequest;
import com.gymflow.pro.dto.response.SupplierResponse;
import com.gymflow.pro.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplierMapper {

    Supplier toEntity(SupplierRequest request);

    SupplierResponse toResponse(Supplier supplier);

    void updateEntity(SupplierRequest request, @MappingTarget Supplier supplier);
}
