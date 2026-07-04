package com.gymflow.pro.dto.response;

import com.gymflow.pro.entity.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    private UUID id;
    private String name;
    private String photoUrl;
    private String cpf;
    private String rg;
    private String gender;
    private String phone;
    private String email;
    private String zipCode;
    private String address;
    private String addressNumber;
    private String addressComplement;
    private String neighborhood;
    private String city;
    private String state;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private LocalDate birthDate;
    private String notes;
    private StudentStatus status;
    private String registrationCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
