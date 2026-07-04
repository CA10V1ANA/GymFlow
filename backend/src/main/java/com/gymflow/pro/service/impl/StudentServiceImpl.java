package com.gymflow.pro.service.impl;

import com.gymflow.pro.dto.request.StudentRequest;
import com.gymflow.pro.dto.response.EnrollmentResponse;
import com.gymflow.pro.dto.response.StudentResponse;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.StudentStatus;
import com.gymflow.pro.exception.BusinessException;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.EnrollmentMapper;
import com.gymflow.pro.mapper.StudentMapper;
import com.gymflow.pro.repository.EnrollmentRepository;
import com.gymflow.pro.repository.StudentRepository;
import com.gymflow.pro.repository.specification.StudentSpecifications;
import com.gymflow.pro.service.StudentService;
import com.gymflow.pro.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentMapper studentMapper;
    private final EnrollmentMapper enrollmentMapper;

    @Override
    public Page<StudentResponse> search(String search, StudentStatus status, Pageable pageable) {
        return studentRepository.findAll(StudentSpecifications.withFilters(search, status), pageable)
                .map(studentMapper::toResponse);
    }

    @Override
    public StudentResponse findById(UUID id) {
        return studentMapper.toResponse(getStudentOrThrow(id));
    }

    @Override
    @Transactional
    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByCpf(request.getCpf())) {
            throw new BusinessException("A student with this CPF is already registered");
        }
        Student student = studentMapper.toEntity(request);
        if (request.getStatus() == null) {
            student.setStatus(StudentStatus.ACTIVE);
        }
        student.setRegistrationCode(generateUniqueRegistrationCode());
        return studentMapper.toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional
    public StudentResponse update(UUID id, StudentRequest request) {
        Student student = getStudentOrThrow(id);
        if (!student.getCpf().equals(request.getCpf()) && studentRepository.existsByCpf(request.getCpf())) {
            throw new BusinessException("A student with this CPF is already registered");
        }
        studentMapper.updateEntity(request, student);
        return studentMapper.toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Student student = getStudentOrThrow(id);
        student.setStatus(StudentStatus.INACTIVE);
        studentRepository.save(student);
    }

    @Override
    public List<EnrollmentResponse> enrollmentHistory(UUID studentId) {
        Student student = getStudentOrThrow(studentId);
        return enrollmentRepository.findByStudentOrderByCreatedAtDesc(student).stream()
                .map(enrollmentMapper::toResponse)
                .toList();
    }

    private Student getStudentOrThrow(UUID id) {
        return studentRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Student", id));
    }

    private String generateUniqueRegistrationCode() {
        String code;
        do {
            code = CodeGenerator.numericCode("S", 8);
        } while (studentRepository.findByRegistrationCode(code).isPresent());
        return code;
    }
}
