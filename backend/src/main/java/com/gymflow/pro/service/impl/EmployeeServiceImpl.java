package com.gymflow.pro.service.impl;

import com.gymflow.pro.dto.request.EmployeeRequest;
import com.gymflow.pro.dto.response.EmployeeResponse;
import com.gymflow.pro.entity.Employee;
import com.gymflow.pro.entity.User;
import com.gymflow.pro.entity.enums.StudentStatus;
import com.gymflow.pro.exception.BusinessException;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.EmployeeMapper;
import com.gymflow.pro.repository.EmployeeRepository;
import com.gymflow.pro.repository.UserRepository;
import com.gymflow.pro.repository.specification.EmployeeSpecifications;
import com.gymflow.pro.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<EmployeeResponse> findAll(String search, Pageable pageable) {
        return employeeRepository.findAll(EmployeeSpecifications.withSearch(search), pageable)
                .map(employeeMapper::toResponse);
    }

    @Override
    public EmployeeResponse findById(UUID id) {
        return employeeMapper.toResponse(getEmployeeOrThrow(id));
    }

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("A user with this email already exists");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("Password is required to create an employee account");
        }
        if (request.getCpf() != null && employeeRepository.existsByCpf(request.getCpf())) {
            throw new BusinessException("An employee with this CPF is already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .active(true)
                .build();
        user = userRepository.save(user);

        Employee employee = Employee.builder()
                .user(user)
                .position(request.getPosition())
                .hiredAt(request.getHiredAt())
                .salary(request.getSalary())
                .cpf(request.getCpf())
                .status(StudentStatus.ACTIVE)
                .build();

        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponse update(UUID id, EmployeeRequest request) {
        Employee employee = getEmployeeOrThrow(id);
        User user = employee.getUser();

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("A user with this email already exists");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        employee.setPosition(request.getPosition());
        employee.setHiredAt(request.getHiredAt());
        employee.setSalary(request.getSalary());
        employee.setCpf(request.getCpf());

        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Employee employee = getEmployeeOrThrow(id);
        employee.setStatus(StudentStatus.INACTIVE);
        employee.getUser().setActive(false);
        employeeRepository.save(employee);
    }

    private Employee getEmployeeOrThrow(UUID id) {
        return employeeRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Employee", id));
    }
}
