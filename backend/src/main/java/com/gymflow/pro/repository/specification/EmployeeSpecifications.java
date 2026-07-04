package com.gymflow.pro.repository.specification;

import com.gymflow.pro.entity.Employee;
import com.gymflow.pro.entity.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> withSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            Join<Employee, User> user = root.join("user");
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(user.get("name")), pattern),
                    cb.like(cb.lower(user.get("email")), pattern),
                    cb.like(root.get("position"), pattern));
        };
    }
}
