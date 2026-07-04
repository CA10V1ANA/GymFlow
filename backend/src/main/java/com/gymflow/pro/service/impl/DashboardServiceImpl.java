package com.gymflow.pro.service.impl;

import com.gymflow.pro.dto.response.DashboardSummaryResponse;
import com.gymflow.pro.entity.enums.StudentStatus;
import com.gymflow.pro.entity.enums.TransactionStatus;
import com.gymflow.pro.entity.enums.TransactionType;
import com.gymflow.pro.repository.AttendanceRepository;
import com.gymflow.pro.repository.EnrollmentRepository;
import com.gymflow.pro.repository.FinancialTransactionRepository;
import com.gymflow.pro.repository.StockMovementRepository;
import com.gymflow.pro.repository.StudentRepository;
import com.gymflow.pro.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    public DashboardSummaryResponse summary() {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        long totalStudents = studentRepository.count();
        long newStudentsThisMonth = studentRepository.countByCreatedAtGreaterThanEqual(currentMonth.atDay(1).atStartOfDay());
        long activeStudents = studentRepository.countByStatus(StudentStatus.ACTIVE);
        long inactiveStudents = studentRepository.countByStatus(StudentStatus.INACTIVE);
        long overdueTransactions = financialTransactionRepository.countByStatusAndDueDateBefore(TransactionStatus.PENDING, today)
                + financialTransactionRepository.countByStatusAndDueDateBefore(TransactionStatus.OVERDUE, today.plusDays(1));

        BigDecimal monthlyRevenue = financialTransactionRepository.sumPaidByTypeAndPeriod(
                TransactionType.INCOME, currentMonth.atDay(1).atStartOfDay(), currentMonth.atEndOfMonth().atTime(LocalTime.MAX));
        BigDecimal annualRevenue = financialTransactionRepository.sumPaidByTypeAndPeriod(
                TransactionType.INCOME, LocalDate.of(today.getYear(), 1, 1).atStartOfDay(),
                LocalDate.of(today.getYear(), 12, 31).atTime(LocalTime.MAX));

        long dailyAttendance = attendanceRepository.countByCheckInBetween(today.atStartOfDay(), today.atTime(LocalTime.MAX));
        LocalDate weekStart = today.minusDays(6);
        long weeklyAttendance = attendanceRepository.countByCheckInBetween(weekStart.atStartOfDay(), today.atTime(LocalTime.MAX));

        Map<String, Long> studentsByPlan = new LinkedHashMap<>();
        enrollmentRepository.countActiveGroupedByPlan()
                .forEach(row -> studentsByPlan.put(row.getPlanName(), row.getTotal()));

        List<DashboardSummaryResponse.TopProductResponse> topProducts = stockMovementRepository
                .findTopSellingProducts(PageRequest.of(0, 5))
                .stream()
                .map(row -> DashboardSummaryResponse.TopProductResponse.builder()
                        .productName(row.getProductName())
                        .totalSold(row.getTotalSold())
                        .build())
                .toList();

        List<DashboardSummaryResponse.MonthlyRevenuePoint> revenueSeries = buildRevenueSeries();

        return DashboardSummaryResponse.builder()
                .totalStudents(totalStudents)
                .newStudentsThisMonth(newStudentsThisMonth)
                .activeStudents(activeStudents)
                .inactiveStudents(inactiveStudents)
                .overdueTransactions(overdueTransactions)
                .monthlyRevenue(monthlyRevenue)
                .annualRevenue(annualRevenue)
                .dailyAttendance(dailyAttendance)
                .weeklyAttendance(weeklyAttendance)
                .studentsByPlan(studentsByPlan)
                .topProducts(topProducts)
                .revenueLastTwelveMonths(revenueSeries)
                .build();
    }

    private List<DashboardSummaryResponse.MonthlyRevenuePoint> buildRevenueSeries() {
        YearMonth currentMonth = YearMonth.now();
        Map<String, BigDecimal> byMonth = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 11; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            byMonth.put(month.format(formatter), BigDecimal.ZERO);
        }

        LocalDateTime since = currentMonth.minusMonths(11).atDay(1).atStartOfDay();
        financialTransactionRepository.monthlyIncomeSince(since).forEach(row -> {
            String key = row.getMonth().toLocalDateTime().format(formatter);
            byMonth.put(key, row.getTotal());
        });

        return byMonth.entrySet().stream()
                .map(entry -> DashboardSummaryResponse.MonthlyRevenuePoint.builder()
                        .month(entry.getKey())
                        .revenue(entry.getValue())
                        .build())
                .toList();
    }
}
