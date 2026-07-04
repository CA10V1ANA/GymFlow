package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.ProductResponse;
import com.gymflow.pro.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductCategoryMapper.class, SupplierMapper.class})
public interface ProductMapper {

    @Mapping(target = "lowStock", expression = "java(product.isLowStock())")
    ProductResponse toResponse(Product product);
}
