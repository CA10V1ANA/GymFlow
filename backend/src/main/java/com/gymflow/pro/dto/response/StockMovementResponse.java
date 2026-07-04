package com.gymflow.pro.dto.response;

import com.gymflow.pro.entity.enums.StockMovementReason;
import com.gymflow.pro.entity.enums.StockMovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private StockMovementType type;
    private Integer quantity;
    private BigDecimal unitPrice;
    private StockMovementReason reason;
    private StudentSummaryResponse student;
    private String createdByName;
    private LocalDateTime createdAt;
}
