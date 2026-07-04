package com.gymflow.pro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCategoryRequest {

    @NotBlank(message = "Name is required")
    private String name;
}
