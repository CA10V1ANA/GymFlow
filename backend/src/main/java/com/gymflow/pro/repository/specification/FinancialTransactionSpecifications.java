package com.gymflow.pro.repository.specification;

import com.gymflow.pro.entity.FinancialTransaction;
import com.gymflow.pro.entity.enums.TransactionCategory;
import com.gymflow.pro.entity.enums.TransactionStatus;
import com.gymflow.pro.entity.enums.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class FinancialTransactionSpecifications {

    private FinancialTransactionSpecifications() {
    }

    public static Specification<FinancialTransaction> withFilters(TransactionType type, TransactionStatus status,
                                                                    TransactionCategory category,
                                                                    LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (type != null) {
                predicate = cb.and(predicate, cb.equal(root.get("type"), type));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (category != null) {
                predicate = cb.and(predicate, cb.equal(root.get("category"), category));
            }
            if (startDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("dueDate"), startDate));
            }
            if (endDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("dueDate"), endDate));
            }
            return predicate;
        };
    }
}
