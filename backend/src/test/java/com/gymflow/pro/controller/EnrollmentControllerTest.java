package com.gymflow.pro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymflow.pro.config.SecurityConfig;
import com.gymflow.pro.dto.request.CancelEnrollmentRequest;
import com.gymflow.pro.dto.request.EnrollmentRequest;
import com.gymflow.pro.dto.response.EnrollmentResponse;
import com.gymflow.pro.entity.enums.EnrollmentStatus;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.security.CustomUserDetailsService;
import com.gymflow.pro.security.JwtTokenProvider;
import com.gymflow.pro.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnrollmentController.class)
@Import(SecurityConfig.class)
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private EnrollmentRequest validRequest() {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(UUID.randomUUID());
        request.setPlanId(UUID.randomUUID());
        request.setPricePaid(BigDecimal.valueOf(99.90));
        return request;
    }

    private EnrollmentResponse sampleResponse(UUID id) {
        return EnrollmentResponse.builder()
                .id(id)
                .status(EnrollmentStatus.ACTIVE)
                .pricePaid(BigDecimal.valueOf(99.90))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAll_withAllowedRole_returnsOk() throws Exception {
        Page<EnrollmentResponse> page = new PageImpl<>(List.of(sampleResponse(UUID.randomUUID())));
        when(enrollmentService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void findAll_withInsufficientRole_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/enrollments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void findById_withAllowedRole_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(enrollmentService.findById(eq(id))).thenReturn(sampleResponse(id));

        mockMvc.perform(get("/api/enrollments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(enrollmentService.findById(eq(id))).thenThrow(ResourceNotFoundException.of("Enrollment", id));

        mockMvc.perform(get("/api/enrollments/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_withValidBody_returnsCreated() throws Exception {
        EnrollmentResponse response = sampleResponse(UUID.randomUUID());
        when(enrollmentService.create(any(EnrollmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/enrollments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void create_withInsufficientRole_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/enrollments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_withMissingRequiredField_returnsBadRequest() throws Exception {
        EnrollmentRequest invalid = validRequest();
        invalid.setStudentId(null);

        mockMvc.perform(post("/api/enrollments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.studentId").exists());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void renew_withValidBody_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        EnrollmentResponse response = sampleResponse(id);
        when(enrollmentService.renew(eq(id), any(EnrollmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/enrollments/{id}/renew", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancel_withValidBody_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        EnrollmentResponse response = sampleResponse(id);
        when(enrollmentService.cancel(eq(id), eq("no longer needed"))).thenReturn(response);

        CancelEnrollmentRequest request = new CancelEnrollmentRequest();
        request.setReason("no longer needed");

        mockMvc.perform(post("/api/enrollments/{id}/cancel", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancel_withMissingReason_returnsBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        CancelEnrollmentRequest request = new CancelEnrollmentRequest();

        mockMvc.perform(post("/api/enrollments/{id}/cancel", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.reason").exists());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void freeze_withValidId_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(enrollmentService.freeze(eq(id))).thenReturn(sampleResponse(id));

        mockMvc.perform(post("/api/enrollments/{id}/freeze", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void reactivate_withValidId_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(enrollmentService.reactivate(eq(id))).thenReturn(sampleResponse(id));

        mockMvc.perform(post("/api/enrollments/{id}/reactivate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void freeze_withInsufficientRole_returnsForbidden() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/api/enrollments/{id}/freeze", id))
                .andExpect(status().isForbidden());
    }
}
