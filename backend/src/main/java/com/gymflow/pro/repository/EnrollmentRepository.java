package com.gymflow.pro.repository;

import com.gymflow.pro.entity.Enrollment;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    Page<Enrollment> findByStudentOrderByCreatedAtDesc(Student student, Pageable pageable);

    List<Enrollment> findByStudentOrderByCreatedAtDesc(Student student);

    Optional<Enrollment> findFirstByStudentAndStatusOrderByCreatedAtDesc(Student student, EnrollmentStatus status);

    long countByStatus(EnrollmentStatus status);

    @org.springframework.data.jpa.repository.Query("select e.plan.name as planName, count(e) as total from Enrollment e where e.status = 'ACTIVE' group by e.plan.name")
    List<PlanCount> countActiveGroupedByPlan();

    interface PlanCount {
        String getPlanName();
        Long getTotal();
    }
}
