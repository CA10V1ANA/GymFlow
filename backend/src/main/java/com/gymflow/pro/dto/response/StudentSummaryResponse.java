package com.gymflow.pro.dto.response;

import com.gymflow.pro.entity.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** Lightweight student projection used when embedded inside other responses. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSummaryResponse {

    private UUID id;
    private String name;
    private String registrationCode;
    private StudentStatus status;
}
