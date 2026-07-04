package com.gymflow.pro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelEnrollmentRequest {

    @NotBlank(message = "Cancel reason is required")
    private String reason;
}
