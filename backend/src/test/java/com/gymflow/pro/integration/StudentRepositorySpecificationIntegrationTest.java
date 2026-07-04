package com.gymflow.pro.integration;

import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.StudentStatus;
import com.gymflow.pro.repository.StudentRepository;
import com.gymflow.pro.repository.specification.StudentSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies StudentSpecifications#withFilters against a real Postgres instance:
 * case-insensitive partial name search and exact status filtering.
 */
class StudentRepositorySpecificationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private StudentRepository studentRepository;

    private Student alice;
    private Student bob;
    private Student carol;

    @BeforeEach
    void seedStudents() {
        alice = studentRepository.save(Student.builder()
                .name("Alice Wonderland")
                .cpf("46563414877")
                .phone("+55 11 91111-1111")
                .email("alice@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .status(StudentStatus.ACTIVE)
                .registrationCode("REG-ALICE1")
                .build());

        bob = studentRepository.save(Student.builder()
                .name("Bob Alicante")
                .cpf("59574890694")
                .phone("+55 11 92222-2222")
                .email("bob@example.com")
                .birthDate(LocalDate.of(1985, 3, 15))
                .status(StudentStatus.INACTIVE)
                .registrationCode("REG-BOB001")
                .build());

        carol = studentRepository.save(Student.builder()
                .name("Carol Smith")
                .cpf("83533740641")
                .phone("+55 11 93333-3333")
                .email("carol@example.com")
                .birthDate(LocalDate.of(1992, 7, 30))
                .status(StudentStatus.ACTIVE)
                .registrationCode("REG-CAROL1")
                .build());
    }

    @Test
    void withFilters_searchByPartialCaseInsensitiveName_matchesMultipleStudents() {
        // "alic" should match both "Alice Wonderland" and "Bob ALIcante" (case-insensitive, partial).
        Page<Student> result = studentRepository.findAll(
                StudentSpecifications.withFilters("alic", null), PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Student::getName)
                .containsExactlyInAnyOrder("Alice Wonderland", "Bob Alicante");
    }

    @Test
    void withFilters_searchByName_isCaseInsensitive() {
        Page<Student> result = studentRepository.findAll(
                StudentSpecifications.withFilters("CAROL", null), PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Student::getName)
                .containsExactly("Carol Smith");
    }

    @Test
    void withFilters_filterByStatus_returnsOnlyMatchingStatus() {
        Page<Student> result = studentRepository.findAll(
                StudentSpecifications.withFilters(null, StudentStatus.ACTIVE), PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Student::getId)
                .containsExactlyInAnyOrder(alice.getId(), carol.getId());
    }

    @Test
    void withFilters_filterByInactiveStatus_returnsOnlyBob() {
        Page<Student> result = studentRepository.findAll(
                StudentSpecifications.withFilters(null, StudentStatus.INACTIVE), PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Student::getId)
                .containsExactly(bob.getId());
    }

    @Test
    void withFilters_combiningSearchAndStatus_narrowsResults() {
        Page<Student> result = studentRepository.findAll(
                StudentSpecifications.withFilters("alic", StudentStatus.INACTIVE), PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Student::getName)
                .containsExactly("Bob Alicante");
    }

    @Test
    void withFilters_noMatch_returnsEmptyPage() {
        Page<Student> result = studentRepository.findAll(
                StudentSpecifications.withFilters("nonexistent-name-xyz", null), PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void withFilters_noFilters_returnsAllSeededStudents() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Student> result = studentRepository.findAll(StudentSpecifications.withFilters(null, null), pageable);

        List<String> names = result.getContent().stream().map(Student::getName).toList();
        assertThat(names).contains("Alice Wonderland", "Bob Alicante", "Carol Smith");
    }
}
