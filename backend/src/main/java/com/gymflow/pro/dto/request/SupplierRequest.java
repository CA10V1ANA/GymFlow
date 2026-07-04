package com.gymflow.pro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String document;
    private String phone;
    private String email;
    private String address;
}
