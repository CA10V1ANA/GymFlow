package com.gymflow.pro.repository;

import com.gymflow.pro.entity.Attendance;
import com.gymflow.pro.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID>, JpaSpecificationExecutor<Attendance> {

    Optional<Attendance> findFirstByStudentAndCheckOutIsNullOrderByCheckInDesc(Student student);

    long countByStudentAndCheckInBetween(Student student, LocalDateTime start, LocalDateTime end);

    long countByCheckInBetween(LocalDateTime start, LocalDateTime end);

    List<Attendance> findByStudentOrderByCheckInDesc(Student student);

    @Query("select count(distinct a.student.id) from Attendance a where a.checkIn between :start and :end")
    long countDistinctStudentsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
