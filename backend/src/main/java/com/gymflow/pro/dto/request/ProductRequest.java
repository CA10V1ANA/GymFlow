package com.gymflow.pro.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private UUID categoryId;
    private UUID supplierId;

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotNull(message = "Cost price is required")
    @DecimalMin(value = "0.0", message = "Cost price must not be negative")
    private BigDecimal costPrice;

    @NotNull(message = "Sale price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Sale price must be greater than zero")
    private BigDecimal salePrice;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must not be negative")
    private Integer stockQuantity;

    @NotNull(message = "Minimum stock is required")
    @Min(value = 0, message = "Minimum stock must not be negative")
    private Integer minStock;

    private Boolean active;
}
