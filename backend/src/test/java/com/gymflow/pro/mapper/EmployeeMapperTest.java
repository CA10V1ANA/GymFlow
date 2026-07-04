package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.EmployeeResponse;
import com.gymflow.pro.entity.Employee;
import com.gymflow.pro.entity.User;
import com.gymflow.pro.entity.enums.StudentStatus;
import com.gymflow.pro.entity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeMapperTest {

    private EmployeeMapper employeeMapper;

    @BeforeEach
    void setUp() {
        EmployeeMapperImpl impl = new EmployeeMapperImpl();
        ReflectionTestUtils.setField(impl, "userMapper", new UserMapperImpl());
        employeeMapper = impl;
    }

    @Test
    void toResponse_shouldMapEmployeeAndNestedUser() {
        UUID id = UUID.randomUUID();
        User user = User.builder().name("Bob").email("bob@example.com").role(UserRole.INSTRUCTOR).build();
        Employee employee = Employee.builder()
                .id(id)
                .user(user)
                .position("Instructor")
                .hiredAt(LocalDate.of(2020, 1, 1))
                .salary(BigDecimal.valueOf(3000))
                .cpf("52998224725")
                .status(StudentStatus.ACTIVE)
                .build();

        EmployeeResponse response = employeeMapper.toResponse(employee);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getPosition()).isEqualTo("Instructor");
        assertThat(response.getSalary()).isEqualByComparingTo(BigDecimal.valueOf(3000));
        assertThat(response.getUser().getName()).isEqualTo("Bob");
        assertThat(response.getUser().getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void toResponse_shouldReturnNull_whenEmployeeNull() {
        assertThat(employeeMapper.toResponse(null)).isNull();
    }
}
