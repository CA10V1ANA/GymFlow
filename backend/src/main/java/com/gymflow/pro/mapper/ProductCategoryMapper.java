package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.request.ProductCategoryRequest;
import com.gymflow.pro.dto.response.ProductCategoryResponse;
import com.gymflow.pro.entity.ProductCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductCategoryMapper {

    ProductCategory toEntity(ProductCategoryRequest request);

    ProductCategoryResponse toResponse(ProductCategory category);
}
