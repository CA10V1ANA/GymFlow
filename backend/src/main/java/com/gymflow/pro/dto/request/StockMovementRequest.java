package com.gymflow.pro.dto.request;

import com.gymflow.pro.entity.enums.StockMovementReason;
import com.gymflow.pro.entity.enums.StockMovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class StockMovementRequest {

    @NotNull(message = "Product is required")
    private UUID productId;

    @NotNull(message = "Movement type is required")
    private StockMovementType type;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    private BigDecimal unitPrice;

    @NotNull(message = "Reason is required")
    private StockMovementReason reason;

    private UUID studentId;
}
