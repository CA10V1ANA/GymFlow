package com.gymflow.pro.integration;

import com.gymflow.pro.dto.request.EnrollmentRequest;
import com.gymflow.pro.dto.request.LoginRequest;
import com.gymflow.pro.dto.response.AuthResponse;
import com.gymflow.pro.dto.response.EnrollmentResponse;
import com.gymflow.pro.entity.Plan;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.FinancialTransaction;
import com.gymflow.pro.entity.enums.EnrollmentStatus;
import com.gymflow.pro.entity.enums.StudentStatus;
import com.gymflow.pro.entity.enums.TransactionCategory;
import com.gymflow.pro.entity.enums.TransactionStatus;
import com.gymflow.pro.entity.enums.TransactionType;
import com.gymflow.pro.repository.FinancialTransactionRepository;
import com.gymflow.pro.repository.PlanRepository;
import com.gymflow.pro.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the real enrollment creation flow (controller -> service -> repositories)
 * and asserts that a pending monthly-fee FinancialTransaction is created, per
 * EnrollmentServiceImpl#createMonthlyFeeTransaction.
 */
class EnrollmentIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    private HttpHeaders authHeaders;

    @BeforeEach
    void authenticateAsAdmin() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@gymflow.com");
        loginRequest.setPassword("Admin@123");

        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                "/api/auth/login", loginRequest, AuthResponse.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(loginResponse.getBody().getAccessToken());
    }

    private Student createStudent(String cpf, String name) {
        Student student = Student.builder()
                .name(name)
                .cpf(cpf)
                .phone("+55 11 90000-0000")
                .email(name.toLowerCase().replace(" ", ".") + "@example.com")
                .birthDate(LocalDate.of(1995, 5, 20))
                .status(StudentStatus.ACTIVE)
                .registrationCode("REG-" + cpf.substring(0, 6))
                .build();
        return studentRepository.save(student);
    }

    @Test
    void createEnrollment_forSeededMensalPlan_createsPendingFinancialTransaction() {
        Student student = createStudent("11070434175", "Maria Enrollment Test");
        Plan mensalPlan = planRepository.findAll().stream()
                .filter(p -> p.getName().equals("Mensal"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seeded 'Mensal' plan not found"));

        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(student.getId());
        request.setPlanId(mensalPlan.getId());
        request.setStartDate(LocalDate.now());

        ResponseEntity<EnrollmentResponse> response = restTemplate.postForEntity(
                "/api/enrollments", new HttpEntity<>(request, authHeaders), EnrollmentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        EnrollmentResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(body.getStudent().getId()).isEqualTo(student.getId());
        assertThat(body.getPlan().getId()).isEqualTo(mensalPlan.getId());
        // Mensal plan has 0% discount, so pricePaid should equal the plan price.
        assertThat(body.getPricePaid()).isEqualByComparingTo(mensalPlan.getPrice());

        List<FinancialTransaction> transactions = financialTransactionRepository.findAll().stream()
                .filter(t -> t.getStudent() != null && t.getStudent().getId().equals(student.getId()))
                .toList();

        assertThat(transactions).hasSize(1);
        FinancialTransaction transaction = transactions.get(0);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(transaction.getCategory()).isEqualTo(TransactionCategory.MONTHLY_FEE);
        assertThat(transaction.getEnrollment().getId()).isEqualTo(body.getId());
        assertThat(transaction.getAmount()).isEqualByComparingTo(mensalPlan.getPrice());
    }

    @Test
    void createEnrollment_whenStudentAlreadyHasActiveEnrollment_isRejected() {
        Student student = createStudent("32892184061", "Joao Duplicate Test");
        Plan mensalPlan = planRepository.findAll().stream()
                .filter(p -> p.getName().equals("Mensal"))
                .findFirst()
                .orElseThrow();

        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(student.getId());
        request.setPlanId(mensalPlan.getId());

        ResponseEntity<EnrollmentResponse> first = restTemplate.postForEntity(
                "/api/enrollments", new HttpEntity<>(request, authHeaders), EnrollmentResponse.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> second = restTemplate.postForEntity(
                "/api/enrollments", new HttpEntity<>(request, authHeaders), String.class);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void createEnrollment_withCustomPricePaid_overridesCalculatedPrice() {
        Student student = createStudent("24475771099", "Ana Custom Price Test");
        Plan mensalPlan = planRepository.findAll().stream()
                .filter(p -> p.getName().equals("Mensal"))
                .findFirst()
                .orElseThrow();

        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(student.getId());
        request.setPlanId(mensalPlan.getId());
        request.setPricePaid(new BigDecimal("99.90"));

        ResponseEntity<EnrollmentResponse> response = restTemplate.postForEntity(
                "/api/enrollments", new HttpEntity<>(request, authHeaders), EnrollmentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getPricePaid()).isEqualByComparingTo("99.90");

        FinancialTransaction transaction = financialTransactionRepository.findAll().stream()
                .filter(t -> t.getStudent() != null && t.getStudent().getId().equals(student.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(transaction.getAmount()).isEqualByComparingTo("99.90");
    }
}
