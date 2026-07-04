package com.gymflow.pro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymflow.pro.config.SecurityConfig;
import com.gymflow.pro.dto.request.StudentRequest;
import com.gymflow.pro.dto.response.StudentResponse;
import com.gymflow.pro.entity.enums.StudentStatus;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.security.CustomUserDetailsService;
import com.gymflow.pro.security.JwtTokenProvider;
import com.gymflow.pro.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@Import(SecurityConfig.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private StudentRequest validRequest() {
        StudentRequest request = new StudentRequest();
        request.setName("Jane Doe");
        request.setCpf("529.982.247-25");
        request.setPhone("11999998888");
        request.setEmail("jane@example.com");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        return request;
    }

    private StudentResponse sampleResponse(UUID id) {
        return StudentResponse.builder()
                .id(id)
                .name("Jane Doe")
                .cpf("52998224725")
                .phone("11999998888")
                .email("jane@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .status(StudentStatus.ACTIVE)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_withAllowedRole_returnsOk() throws Exception {
        Page<StudentResponse> page = new PageImpl<>(List.of(sampleResponse(UUID.randomUUID())));
        when(studentService.search(any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Jane Doe"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void search_withInsufficientRole_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void findById_withAllowedStudentRole_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(studentService.findById(eq(id))).thenReturn(sampleResponse(id));

        mockMvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(studentService.findById(eq(id))).thenThrow(ResourceNotFoundException.of("Student", id));

        mockMvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void create_withValidBody_returnsCreated() throws Exception {
        StudentResponse response = sampleResponse(UUID.randomUUID());
        when(studentService.create(any(StudentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/students")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void create_withInsufficientRole_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_withMissingRequiredField_returnsBadRequest() throws Exception {
        StudentRequest invalid = validRequest();
        invalid.setName(null);

        mockMvc.perform(post("/api/students")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_withValidBody_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        StudentResponse response = sampleResponse(id);
        when(studentService.update(eq(id), any(StudentRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/students/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_withValidId_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/students/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void delete_withInsufficientRole_returnsForbidden() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/students/{id}", id))
                .andExpect(status().isForbidden());
    }
}
