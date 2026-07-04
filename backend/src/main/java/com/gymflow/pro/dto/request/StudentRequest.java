package com.gymflow.pro.dto.request;

import com.gymflow.pro.entity.enums.StudentStatus;
import com.gymflow.pro.validation.CPF;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StudentRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    private String name;

    private String photoUrl;

    @NotBlank(message = "CPF is required")
    @CPF
    private String cpf;

    private String rg;

    private String gender;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String zipCode;
    private String address;
    private String addressNumber;
    private String addressComplement;
    private String neighborhood;
    private String city;

    @Size(max = 2)
    private String state;

    private String emergencyContactName;
    private String emergencyContactPhone;

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    private String notes;

    private StudentStatus status;
}
