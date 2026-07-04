package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.StockMovementResponse;
import com.gymflow.pro.entity.StockMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = StudentMapper.class)
public interface StockMovementMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "student", source = "student")
    @Mapping(target = "createdByName", source = "createdBy.name")
    StockMovementResponse toResponse(StockMovement stockMovement);
}
