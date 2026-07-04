package com.gymflow.pro.dto.request;

import com.gymflow.pro.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarkAsPaidRequest {

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
