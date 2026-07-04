package com.gymflow.pro.repository.specification;

import com.gymflow.pro.entity.Attendance;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class AttendanceSpecifications {

    private AttendanceSpecifications() {
    }

    public static Specification<Attendance> withStudent(UUID studentId) {
        return (root, query, cb) -> studentId == null
                ? cb.conjunction()
                : cb.equal(root.get("student").get("id"), studentId);
    }
}
