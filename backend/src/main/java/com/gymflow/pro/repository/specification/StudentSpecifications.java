package com.gymflow.pro.repository.specification;

import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.enums.StudentStatus;
import org.springframework.data.jpa.domain.Specification;

public final class StudentSpecifications {

    private StudentSpecifications() {
    }

    public static Specification<Student> withFilters(String search, StudentStatus status) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(root.get("cpf"), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(root.get("registrationCode"), pattern)));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            return predicate;
        };
    }
}
