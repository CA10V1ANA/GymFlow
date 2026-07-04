package com.gymflow.pro.dto.response;

import com.gymflow.pro.entity.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private UUID id;
    private UserResponse user;
    private String position;
    private LocalDate hiredAt;
    private BigDecimal salary;
    private String cpf;
    private StudentStatus status;
}
