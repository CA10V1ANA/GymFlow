package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.CheckInRequest;
import com.gymflow.pro.dto.response.AttendanceResponse;
import com.gymflow.pro.entity.Attendance;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.AttendanceMethod;
import com.gymflow.pro.exception.BusinessException;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.AttendanceMapper;
import com.gymflow.pro.repository.AttendanceRepository;
import com.gymflow.pro.repository.StudentRepository;
import com.gymflow.pro.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AttendanceMapper attendanceMapper;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private UUID studentId;
    private Student student;
    private String registrationCode;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        registrationCode = "REG-001";
        student = Student.builder().id(studentId).name("Jane Doe").build();
    }

    @Test
    void checkIn_shouldCreateAttendance_whenNoOpenCheckInExists() {
        CheckInRequest request = new CheckInRequest();
        request.setRegistrationCode(registrationCode);
        request.setMethod(AttendanceMethod.QR_CODE);

        AttendanceResponse response = AttendanceResponse.builder().id(UUID.randomUUID()).build();

        when(studentRepository.findByRegistrationCode(registrationCode)).thenReturn(Optional.of(student));
        when(attendanceRepository.findFirstByStudentAndCheckOutIsNullOrderByCheckInDesc(student))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(attendanceMapper.toResponse(any(Attendance.class))).thenReturn(response);

        AttendanceResponse result = attendanceService.checkIn(request);

        assertThat(result).isEqualTo(response);

        ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceRepository).save(captor.capture());
        Attendance saved = captor.getValue();
        assertThat(saved.getStudent()).isEqualTo(student);
        assertThat(saved.getMethod()).isEqualTo(AttendanceMethod.QR_CODE);
        assertThat(saved.getCheckIn()).isNotNull();
        assertThat(saved.getCheckOut()).isNull();
    }

    @Test
    void checkIn_shouldDefaultMethodToCode_whenMethodNotProvided() {
        CheckInRequest request = new CheckInRequest();
        request.setRegistrationCode(registrationCode);

        when(studentRepository.findByRegistrationCode(registrationCode)).thenReturn(Optional.of(student));
        when(attendanceRepository.findFirstByStudentAndCheckOutIsNullOrderByCheckInDesc(student))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(attendanceMapper.toResponse(any(Attendance.class))).thenReturn(AttendanceResponse.builder().build());

        attendanceService.checkIn(request);

        ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceRepository).save(captor.capture());
        assertThat(captor.getValue().getMethod()).isEqualTo(AttendanceMethod.CODE);
    }

    @Test
    void checkIn_shouldThrowResourceNotFound_whenStudentDoesNotExist() {
        CheckInRequest request = new CheckInRequest();
        request.setRegistrationCode("UNKNOWN");

        when(studentRepository.findByRegistrationCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.checkIn(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("UNKNOWN");

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void checkIn_shouldThrowBusinessException_whenStudentAlreadyHasOpenCheckIn() {
        CheckInRequest request = new CheckInRequest();
        request.setRegistrationCode(registrationCode);

        Attendance openAttendance = Attendance.builder().student(student).checkIn(LocalDateTime.now()).build();

        when(studentRepository.findByRegistrationCode(registrationCode)).thenReturn(Optional.of(student));
        when(attendanceRepository.findFirstByStudentAndCheckOutIsNullOrderByCheckInDesc(student))
                .thenReturn(Optional.of(openAttendance));

        assertThatThrownBy(() -> attendanceService.checkIn(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already has an open check-in");

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void checkOut_shouldSetCheckOutTime_whenOpenAttendanceExists() {
        Attendance openAttendance = Attendance.builder().student(student).checkIn(LocalDateTime.now().minusHours(1))
                .build();
        AttendanceResponse response = AttendanceResponse.builder().id(UUID.randomUUID()).build();

        when(studentRepository.findByRegistrationCode(registrationCode)).thenReturn(Optional.of(student));
        when(attendanceRepository.findFirstByStudentAndCheckOutIsNullOrderByCheckInDesc(student))
                .thenReturn(Optional.of(openAttendance));
        when(attendanceRepository.save(openAttendance)).thenReturn(openAttendance);
        when(attendanceMapper.toResponse(openAttendance)).thenReturn(response);

        AttendanceResponse result = attendanceService.checkOut(registrationCode);

        assertThat(result).isEqualTo(response);
        assertThat(openAttendance.getCheckOut()).isNotNull();
        verify(attendanceRepository).save(openAttendance);
    }

    @Test
    void checkOut_shouldThrowResourceNotFound_whenStudentDoesNotExist() {
        when(studentRepository.findByRegistrationCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.checkOut("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void checkOut_shouldThrowBusinessException_whenNoOpenCheckInExists() {
        when(studentRepository.findByRegistrationCode(registrationCode)).thenReturn(Optional.of(student));
        when(attendanceRepository.findFirstByStudentAndCheckOutIsNullOrderByCheckInDesc(student))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.checkOut(registrationCode))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No open check-in found");

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void findAll_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Attendance attendance = Attendance.builder().student(student).build();
        Page<Attendance> page = new PageImpl<>(List.of(attendance));
        AttendanceResponse response = AttendanceResponse.builder().id(UUID.randomUUID()).build();

        when(attendanceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(attendanceMapper.toResponse(attendance)).thenReturn(response);

        Page<AttendanceResponse> result = attendanceService.findAll(studentId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(response);
    }

    @Test
    void historyByStudent_shouldReturnMappedList_whenStudentExists() {
        Attendance attendance = Attendance.builder().student(student).build();
        AttendanceResponse response = AttendanceResponse.builder().id(UUID.randomUUID()).build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(attendanceRepository.findByStudentOrderByCheckInDesc(student)).thenReturn(List.of(attendance));
        when(attendanceMapper.toResponse(attendance)).thenReturn(response);

        List<AttendanceResponse> result = attendanceService.historyByStudent(studentId);

        assertThat(result).containsExactly(response);
    }

    @Test
    void historyByStudent_shouldThrow_whenStudentNotFound() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.historyByStudent(studentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void monthlyFrequency_shouldReturnCount_whenStudentExists() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(attendanceRepository.countByStudentAndCheckInBetween(eq(student), any(), any())).thenReturn(7L);

        long result = attendanceService.monthlyFrequency(studentId);

        assertThat(result).isEqualTo(7L);
    }

    @Test
    void monthlyFrequency_shouldThrow_whenStudentNotFound() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.monthlyFrequency(studentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void dailyFrequency_shouldReturnCount_whenStudentExists() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(attendanceRepository.countByStudentAndCheckInBetween(eq(student), any(), any())).thenReturn(2L);

        long result = attendanceService.dailyFrequency(studentId);

        assertThat(result).isEqualTo(2L);
    }

    @Test
    void dailyFrequency_shouldThrow_whenStudentNotFound() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.dailyFrequency(studentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
