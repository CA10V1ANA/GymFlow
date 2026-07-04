package com.gymflow.pro.repository;

import com.gymflow.pro.entity.FinancialTransaction;
import com.gymflow.pro.entity.enums.TransactionStatus;
import com.gymflow.pro.entity.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID>,
        JpaSpecificationExecutor<FinancialTransaction> {

    @Query("select coalesce(sum(t.amount - t.discount + t.penalty), 0) from FinancialTransaction t " +
            "where t.type = :type and t.status = 'PAID' and t.paidAt between :start and :end")
    BigDecimal sumPaidByTypeAndPeriod(@Param("type") TransactionType type,
                                       @Param("start") java.time.LocalDateTime start,
                                       @Param("end") java.time.LocalDateTime end);

    long countByStatusAndDueDateBefore(TransactionStatus status, LocalDate date);

    List<FinancialTransaction> findByStatusAndDueDateBefore(TransactionStatus status, LocalDate date);

    @Query("select function('date_trunc', 'month', t.paidAt) as month, sum(t.amount - t.discount + t.penalty) as total " +
            "from FinancialTransaction t where t.type = 'INCOME' and t.status = 'PAID' and t.paidAt >= :since " +
            "group by function('date_trunc', 'month', t.paidAt) order by month")
    List<MonthlyTotal> monthlyIncomeSince(@Param("since") java.time.LocalDateTime since);

    interface MonthlyTotal {
        java.sql.Timestamp getMonth();
        BigDecimal getTotal();
    }
}
