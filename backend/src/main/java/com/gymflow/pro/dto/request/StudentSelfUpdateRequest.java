package com.gymflow.pro.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentSelfUpdateRequest {

    private String phone;

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
}
