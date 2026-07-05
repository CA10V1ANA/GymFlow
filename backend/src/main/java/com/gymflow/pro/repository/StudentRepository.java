package com.gymflow.pro.repository;

import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    Optional<Student> findByRegistrationCode(String registrationCode);

    Optional<Student> findByUserId(UUID userId);

    long countByStatus(StudentStatus status);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);
}
