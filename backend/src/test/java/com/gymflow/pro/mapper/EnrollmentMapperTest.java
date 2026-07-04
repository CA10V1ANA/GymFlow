package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.EnrollmentResponse;
import com.gymflow.pro.entity.Enrollment;
import com.gymflow.pro.entity.Plan;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.EnrollmentStatus;
import com.gymflow.pro.entity.enums.PlanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EnrollmentMapperTest {

    private EnrollmentMapper enrollmentMapper;

    @BeforeEach
    void setUp() {
        EnrollmentMapperImpl impl = new EnrollmentMapperImpl();
        ReflectionTestUtils.setField(impl, "studentMapper", new StudentMapperImpl());
        ReflectionTestUtils.setField(impl, "planMapper", new PlanMapperImpl());
        enrollmentMapper = impl;
    }

    @Test
    void toResponse_shouldMapNestedStudentAndPlan() {
        UUID id = UUID.randomUUID();
        Enrollment enrollment = Enrollment.builder()
                .id(id)
                .student(Student.builder().name("Jane").build())
                .plan(Plan.builder().name("Gold").type(PlanType.MONTHLY).build())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .status(EnrollmentStatus.ACTIVE)
                .pricePaid(BigDecimal.valueOf(100))
                .build();

        EnrollmentResponse response = enrollmentMapper.toResponse(enrollment);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getStudent().getName()).isEqualTo("Jane");
        assertThat(response.getPlan().getName()).isEqualTo("Gold");
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
    }

    @Test
    void toResponse_shouldReturnNull_whenEnrollmentNull() {
        assertThat(enrollmentMapper.toResponse(null)).isNull();
    }
}
