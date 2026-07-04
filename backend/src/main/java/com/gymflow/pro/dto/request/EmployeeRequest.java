package com.gymflow.pro.dto.request;

import com.gymflow.pro.entity.enums.UserRole;
import com.gymflow.pro.validation.CPF;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EmployeeRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /** Required when creating a new employee; ignored on update. */
    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;

    private String phone;

    @NotBlank(message = "Position is required")
    private String position;

    @NotNull(message = "Hire date is required")
    private LocalDate hiredAt;

    private BigDecimal salary;

    @CPF
    private String cpf;
}
