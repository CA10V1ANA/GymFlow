package com.gymflow.pro.repository;

import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkoutRepository extends JpaRepository<Workout, UUID> {

    Page<Workout> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Workout> findByStudentOrderByCreatedAtDesc(Student student, Pageable pageable);
}
