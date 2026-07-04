package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.request.StudentRequest;
import com.gymflow.pro.dto.response.StudentResponse;
import com.gymflow.pro.dto.response.StudentSummaryResponse;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.StudentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StudentMapperTest {

    private StudentMapper studentMapper;

    @BeforeEach
    void setUp() {
        studentMapper = new StudentMapperImpl();
    }

    private StudentRequest buildRequest() {
        StudentRequest request = new StudentRequest();
        request.setName("Jane Doe");
        request.setCpf("52998224725");
        request.setPhone("11999998888");
        request.setEmail("jane@example.com");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setStatus(StudentStatus.ACTIVE);
        request.setCity("Sao Paulo");
        request.setState("SP");
        return request;
    }

    @Test
    void toEntity_shouldMapAllFields() {
        StudentRequest request = buildRequest();

        Student entity = studentMapper.toEntity(request);

        assertThat(entity.getName()).isEqualTo("Jane Doe");
        assertThat(entity.getCpf()).isEqualTo("52998224725");
        assertThat(entity.getPhone()).isEqualTo("11999998888");
        assertThat(entity.getEmail()).isEqualTo("jane@example.com");
        assertThat(entity.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(entity.getStatus()).isEqualTo(StudentStatus.ACTIVE);
        assertThat(entity.getCity()).isEqualTo("Sao Paulo");
        assertThat(entity.getState()).isEqualTo("SP");
    }

    @Test
    void toEntity_shouldReturnNull_whenRequestNull() {
        assertThat(studentMapper.toEntity(null)).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        Student student = Student.builder()
                .id(id)
                .name("Jane Doe")
                .cpf("52998224725")
                .phone("11999998888")
                .email("jane@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .status(StudentStatus.ACTIVE)
                .registrationCode("S12345678")
                .build();

        StudentResponse response = studentMapper.toResponse(student);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Jane Doe");
        assertThat(response.getCpf()).isEqualTo("52998224725");
        assertThat(response.getStatus()).isEqualTo(StudentStatus.ACTIVE);
        assertThat(response.getRegistrationCode()).isEqualTo("S12345678");
    }

    @Test
    void toResponse_shouldReturnNull_whenStudentNull() {
        assertThat(studentMapper.toResponse(null)).isNull();
    }

    @Test
    void toSummary_shouldMapKeyFields() {
        UUID id = UUID.randomUUID();
        Student student = Student.builder()
                .id(id)
                .name("Jane Doe")
                .registrationCode("S12345678")
                .status(StudentStatus.ACTIVE)
                .build();

        StudentSummaryResponse summary = studentMapper.toSummary(student);

        assertThat(summary.getId()).isEqualTo(id);
        assertThat(summary.getName()).isEqualTo("Jane Doe");
        assertThat(summary.getRegistrationCode()).isEqualTo("S12345678");
        assertThat(summary.getStatus()).isEqualTo(StudentStatus.ACTIVE);
    }

    @Test
    void toSummary_shouldReturnNull_whenStudentNull() {
        assertThat(studentMapper.toSummary(null)).isNull();
    }

    @Test
    void updateEntity_shouldOnlyOverwriteNonNullFields() {
        Student student = Student.builder()
                .name("Old Name")
                .cpf("11111111111")
                .phone("11000000000")
                .email("old@example.com")
                .city("Old City")
                .build();

        StudentRequest request = new StudentRequest();
        request.setName("New Name");
        request.setCpf("52998224725");
        request.setPhone("11999998888");
        request.setEmail("new@example.com");

        studentMapper.updateEntity(request, student);

        assertThat(student.getName()).isEqualTo("New Name");
        assertThat(student.getCpf()).isEqualTo("52998224725");
        assertThat(student.getCity()).isEqualTo("Old City");
    }

    @Test
    void updateEntity_shouldDoNothing_whenRequestNull() {
        Student student = Student.builder().name("Old Name").build();

        studentMapper.updateEntity(null, student);

        assertThat(student.getName()).isEqualTo("Old Name");
    }
}
