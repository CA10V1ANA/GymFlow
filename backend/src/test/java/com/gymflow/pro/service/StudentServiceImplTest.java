package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.StudentRequest;
import com.gymflow.pro.dto.response.EnrollmentResponse;
import com.gymflow.pro.dto.response.StudentResponse;
import com.gymflow.pro.entity.Enrollment;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.StudentStatus;
import com.gymflow.pro.exception.BusinessException;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.EnrollmentMapper;
import com.gymflow.pro.mapper.StudentMapper;
import com.gymflow.pro.repository.EnrollmentRepository;
import com.gymflow.pro.repository.StudentRepository;
import com.gymflow.pro.repository.UserRepository;
import com.gymflow.pro.service.impl.StudentServiceImpl;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private EnrollmentMapper enrollmentMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private StudentServiceImpl studentService;

    private Student student;
    private UUID studentId;

    @BeforeEach
    void setUp() {
        studentService = new StudentServiceImpl(
                studentRepository,
                enrollmentRepository,
                userRepository,
                studentMapper,
                enrollmentMapper,
                passwordEncoder);
        studentId = UUID.randomUUID();
        student = Student.builder()
                .id(studentId)
                .name("John Doe")
                .cpf("12345678901")
                .phone("11999999999")
                .email("john@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .status(StudentStatus.ACTIVE)
                .registrationCode("S12345678")
                .build();
    }

    private StudentRequest buildRequest(String cpf) {
        StudentRequest request = new StudentRequest();
        request.setName("John Doe");
        request.setCpf(cpf);
        request.setPhone("11999999999");
        request.setEmail("john@example.com");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        return request;
    }

    @Test
    void search_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Student> studentPage = new PageImpl<>(List.of(student));
        StudentResponse response = StudentResponse.builder().id(studentId).name("John Doe").build();

        when(studentRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(studentPage);
        when(studentMapper.toResponse(student)).thenReturn(response);

        Page<StudentResponse> result = studentService.search("john", StudentStatus.ACTIVE, pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void findById_shouldReturnStudent_whenExists() {
        StudentResponse response = StudentResponse.builder().id(studentId).name("John Doe").build();
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentMapper.toResponse(student)).thenReturn(response);

        StudentResponse result = studentService.findById(studentId);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void findById_shouldThrowResourceNotFound_whenMissing() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findById(studentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldThrowBusinessException_whenCpfDuplicate() {
        StudentRequest request = buildRequest("12345678901");
        when(studentRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> studentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF");

        verify(studentRepository, never()).save(any());
    }

    @Test
    void create_shouldSaveStudent_withGeneratedRegistrationCodeAndActiveStatus_whenStatusNull() {
        StudentRequest request = buildRequest("12345678901");
        request.setStatus(null);
        Student mappedEntity = Student.builder().name("John Doe").cpf("12345678901").build();
        StudentResponse response = StudentResponse.builder().id(studentId).name("John Doe").build();

        when(studentRepository.existsByCpf("12345678901")).thenReturn(false);
        when(studentMapper.toEntity(request)).thenReturn(mappedEntity);
        when(studentRepository.findByRegistrationCode(anyString())).thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentMapper.toResponse(any(Student.class))).thenReturn(response);

        StudentResponse result = studentService.create(request);

        assertThat(result).isEqualTo(response);
        assertThat(mappedEntity.getStatus()).isEqualTo(StudentStatus.ACTIVE);
        assertThat(mappedEntity.getRegistrationCode()).isNotBlank();

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        assertThat(captor.getValue().getRegistrationCode()).isEqualTo(mappedEntity.getRegistrationCode());
    }

    @Test
    void create_shouldNotOverrideStatus_whenStatusProvided() {
        StudentRequest request = buildRequest("12345678901");
        request.setStatus(StudentStatus.PENDING);
        Student mappedEntity = Student.builder().name("John Doe").cpf("12345678901").status(StudentStatus.PENDING).build();
        StudentResponse response = StudentResponse.builder().id(studentId).build();

        when(studentRepository.existsByCpf("12345678901")).thenReturn(false);
        when(studentMapper.toEntity(request)).thenReturn(mappedEntity);
        when(studentRepository.findByRegistrationCode(anyString())).thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentMapper.toResponse(any(Student.class))).thenReturn(response);

        studentService.create(request);

        assertThat(mappedEntity.getStatus()).isEqualTo(StudentStatus.PENDING);
    }

    @Test
    void update_shouldThrowResourceNotFound_whenMissing() {
        StudentRequest request = buildRequest("12345678901");
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.update(studentId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldThrowBusinessException_whenNewCpfBelongsToAnotherStudent() {
        StudentRequest request = buildRequest("99999999999");
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentRepository.existsByCpf("99999999999")).thenReturn(true);

        assertThatThrownBy(() -> studentService.update(studentId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF");

        verify(studentRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameCpf_withoutDuplicateCheck() {
        StudentRequest request = buildRequest(student.getCpf());
        StudentResponse response = StudentResponse.builder().id(studentId).build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);
        when(studentMapper.toResponse(student)).thenReturn(response);

        StudentResponse result = studentService.update(studentId, request);

        assertThat(result).isEqualTo(response);
        verify(studentRepository, never()).existsByCpf(anyString());
        verify(studentMapper).updateEntity(request, student);
        verify(studentRepository).save(student);
    }

    @Test
    void delete_shouldSetStatusInactive_andSave() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);

        studentService.delete(studentId);

        assertThat(student.getStatus()).isEqualTo(StudentStatus.INACTIVE);
        verify(studentRepository).save(student);
    }

    @Test
    void delete_shouldThrowResourceNotFound_whenMissing() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.delete(studentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void enrollmentHistory_shouldReturnMappedList() {
        Enrollment enrollment = Enrollment.builder().id(UUID.randomUUID()).student(student).build();
        EnrollmentResponse enrollmentResponse = EnrollmentResponse.builder().id(enrollment.getId()).build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentOrderByCreatedAtDesc(student)).thenReturn(List.of(enrollment));
        when(enrollmentMapper.toResponse(enrollment)).thenReturn(enrollmentResponse);

        List<EnrollmentResponse> result = studentService.enrollmentHistory(studentId);

        assertThat(result).containsExactly(enrollmentResponse);
    }

    @Test
    void enrollmentHistory_shouldThrowResourceNotFound_whenStudentMissing() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.enrollmentHistory(studentId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(enrollmentRepository, times(0)).findByStudentOrderByCreatedAtDesc(any(Student.class));
    }
}
