package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.EmployeeRequest;
import com.gymflow.pro.dto.response.EmployeeResponse;
import com.gymflow.pro.entity.Employee;
import com.gymflow.pro.entity.User;
import com.gymflow.pro.entity.enums.StudentStatus;
import com.gymflow.pro.entity.enums.UserRole;
import com.gymflow.pro.exception.BusinessException;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.EmployeeMapper;
import com.gymflow.pro.repository.EmployeeRepository;
import com.gymflow.pro.repository.UserRepository;
import com.gymflow.pro.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private EmployeeServiceImpl employeeService;

    private UUID employeeId;
    private User user;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(employeeRepository, userRepository, employeeMapper, passwordEncoder);
        employeeId = UUID.randomUUID();
        user = User.builder().id(UUID.randomUUID()).name("Jane Doe").email("jane@example.com")
                .passwordHash("hashed").role(UserRole.INSTRUCTOR).active(true).build();
        employee = Employee.builder().id(employeeId).user(user).position("Trainer")
                .hiredAt(LocalDate.of(2020, 1, 1)).status(StudentStatus.ACTIVE).build();
    }

    private EmployeeRequest buildRequest() {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPassword("secret123");
        request.setRole(UserRole.INSTRUCTOR);
        request.setPosition("Trainer");
        request.setHiredAt(LocalDate.of(2020, 1, 1));
        request.setSalary(BigDecimal.valueOf(3000));
        return request;
    }

    @Test
    void findAll_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(List.of(employee));
        EmployeeResponse response = EmployeeResponse.builder().id(employeeId).build();

        when(employeeRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        Page<EmployeeResponse> result = employeeService.findAll("jane", pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void findById_shouldReturnEmployee_whenExists() {
        EmployeeResponse response = EmployeeResponse.builder().id(employeeId).build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        EmployeeResponse result = employeeService.findById(employeeId);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void findById_shouldThrowResourceNotFound_whenMissing() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findById(employeeId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldThrowBusinessException_whenEmailAlreadyExists() {
        EmployeeRequest request = buildRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email already exists");

        verify(userRepository, never()).save(any());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBusinessException_whenPasswordBlank() {
        EmployeeRequest request = buildRequest();
        request.setPassword("  ");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        assertThatThrownBy(() -> employeeService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Password is required");

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBusinessException_whenPasswordNull() {
        EmployeeRequest request = buildRequest();
        request.setPassword(null);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        assertThatThrownBy(() -> employeeService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Password is required");
    }

    @Test
    void create_shouldThrowBusinessException_whenCpfAlreadyRegistered() {
        EmployeeRequest request = buildRequest();
        request.setCpf("12345678901");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(employeeRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF");

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldSkipCpfCheck_whenCpfNull() {
        EmployeeRequest request = buildRequest();
        request.setCpf(null);
        EmployeeResponse response = EmployeeResponse.builder().id(employeeId).build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-secret");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(response);

        EmployeeResponse result = employeeService.create(request);

        assertThat(result).isEqualTo(response);
        verify(employeeRepository, never()).existsByCpf(any());
    }

    @Test
    void create_shouldCreateUserAndEmployee_withEncodedPasswordAndActiveStatus() {
        EmployeeRequest request = buildRequest();
        EmployeeResponse response = EmployeeResponse.builder().id(employeeId).build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-secret");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(response);

        EmployeeResponse result = employeeService.create(request);

        assertThat(result).isEqualTo(response);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("jane@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-secret");
        assertThat(savedUser.isActive()).isTrue();
        assertThat(savedUser.getRole()).isEqualTo(UserRole.INSTRUCTOR);

        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(employeeCaptor.capture());
        Employee savedEmployee = employeeCaptor.getValue();
        assertThat(savedEmployee.getPosition()).isEqualTo("Trainer");
        assertThat(savedEmployee.getStatus()).isEqualTo(StudentStatus.ACTIVE);
        assertThat(savedEmployee.getUser()).isEqualTo(savedUser);
    }

    @Test
    void update_shouldThrowResourceNotFound_whenMissing() {
        EmployeeRequest request = buildRequest();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.update(employeeId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldThrowBusinessException_whenNewEmailBelongsToAnotherUser() {
        EmployeeRequest request = buildRequest();
        request.setEmail("new@example.com");
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.update(employeeId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email already exists");

        verify(userRepository, never()).save(any());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameEmail_withoutDuplicateCheck() {
        EmployeeRequest request = buildRequest();
        request.setPassword(null);
        EmployeeResponse response = EmployeeResponse.builder().id(employeeId).build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(userRepository.save(user)).thenReturn(user);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        EmployeeResponse result = employeeService.update(employeeId, request);

        assertThat(result).isEqualTo(response);
        verify(userRepository, never()).existsByEmail(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void update_shouldEncodePassword_whenProvided() {
        EmployeeRequest request = buildRequest();
        request.setPassword("newPassword");
        EmployeeResponse response = EmployeeResponse.builder().id(employeeId).build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(passwordEncoder.encode("newPassword")).thenReturn("new-hashed");
        when(userRepository.save(user)).thenReturn(user);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        employeeService.update(employeeId, request);

        assertThat(user.getPasswordHash()).isEqualTo("new-hashed");
    }

    @Test
    void update_shouldNotChangePassword_whenBlank() {
        EmployeeRequest request = buildRequest();
        request.setPassword(" ");
        String originalHash = user.getPasswordHash();
        EmployeeResponse response = EmployeeResponse.builder().id(employeeId).build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(userRepository.save(user)).thenReturn(user);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        employeeService.update(employeeId, request);

        assertThat(user.getPasswordHash()).isEqualTo(originalHash);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void update_shouldUpdateEmployeeFields() {
        EmployeeRequest request = buildRequest();
        request.setPosition("Head Trainer");
        request.setSalary(BigDecimal.valueOf(5000));
        EmployeeResponse response = EmployeeResponse.builder().id(employeeId).build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(userRepository.save(user)).thenReturn(user);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        employeeService.update(employeeId, request);

        assertThat(employee.getPosition()).isEqualTo("Head Trainer");
        assertThat(employee.getSalary()).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }

    @Test
    void delete_shouldDeactivateEmployeeAndUser() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);

        employeeService.delete(employeeId);

        assertThat(employee.getStatus()).isEqualTo(StudentStatus.INACTIVE);
        assertThat(employee.getUser().isActive()).isFalse();
        verify(employeeRepository).save(employee);
    }

    @Test
    void delete_shouldThrowResourceNotFound_whenMissing() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.delete(employeeId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
