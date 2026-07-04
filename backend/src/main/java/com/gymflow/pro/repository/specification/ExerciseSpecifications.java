package com.gymflow.pro.repository.specification;

import com.gymflow.pro.entity.Exercise;
import com.gymflow.pro.entity.enums.ExerciseCategory;
import com.gymflow.pro.entity.enums.ExerciseLevel;
import com.gymflow.pro.entity.enums.MuscleGroup;
import org.springframework.data.jpa.domain.Specification;

public final class ExerciseSpecifications {

    private ExerciseSpecifications() {
    }

    public static Specification<Exercise> withFilters(ExerciseCategory category, MuscleGroup muscleGroup,
                                                        ExerciseLevel level, String search) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (category != null) {
                predicate = cb.and(predicate, cb.equal(root.get("category"), category));
            }
            if (muscleGroup != null) {
                predicate = cb.and(predicate, cb.equal(root.get("muscleGroup"), muscleGroup));
            }
            if (level != null) {
                predicate = cb.and(predicate, cb.equal(root.get("level"), level));
            }
            if (search != null && !search.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("name")), "%" + search.trim().toLowerCase() + "%"));
            }
            return predicate;
        };
    }
}
