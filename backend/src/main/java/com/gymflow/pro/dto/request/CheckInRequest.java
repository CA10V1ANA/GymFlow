package com.gymflow.pro.dto.request;

import com.gymflow.pro.entity.enums.AttendanceMethod;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckInRequest {

    @NotBlank(message = "Registration code is required")
    private String registrationCode;

    private AttendanceMethod method;
}
