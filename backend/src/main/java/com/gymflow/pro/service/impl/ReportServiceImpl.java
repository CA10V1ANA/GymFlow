package com.gymflow.pro.service.impl;

import com.gymflow.pro.entity.enums.StudentStatus;
import com.gymflow.pro.entity.enums.TransactionStatus;
import com.gymflow.pro.entity.enums.TransactionType;
import com.gymflow.pro.repository.AttendanceRepository;
import com.gymflow.pro.repository.EnrollmentRepository;
import com.gymflow.pro.repository.FinancialTransactionRepository;
import com.gymflow.pro.repository.PlanRepository;
import com.gymflow.pro.repository.ProductRepository;
import com.gymflow.pro.repository.StockMovementRepository;
import com.gymflow.pro.repository.StudentRepository;
import com.gymflow.pro.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides aggregated, report-ready JSON payloads for the admin reporting screens.
 * NOTE: PDF/Excel export is out of scope for this phase — a future ReportExportService
 * would consume these same aggregates and render them with a library such as
 * OpenPDF/Apache POI, so the data here is intentionally structured flat and export-friendly.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PlanRepository planRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    public Map<String, Object> studentsReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("total", studentRepository.count());
        report.put("active", studentRepository.countByStatus(StudentStatus.ACTIVE));
        report.put("inactive", studentRepository.countByStatus(StudentStatus.INACTIVE));
        report.put("pending", studentRepository.countByStatus(StudentStatus.PENDING));
        return report;
    }

    @Override
    public Map<String, Object> financialReport(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        var startDateTime = start.atStartOfDay();
        var endDateTime = end.atTime(LocalTime.MAX);

        var income = financialTransactionRepository.sumPaidByTypeAndPeriod(TransactionType.INCOME, startDateTime, endDateTime);
        var expense = financialTransactionRepository.sumPaidByTypeAndPeriod(TransactionType.EXPENSE, startDateTime, endDateTime);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("periodStart", start);
        report.put("periodEnd", end);
        report.put("totalIncome", income);
        report.put("totalExpense", expense);
        report.put("netResult", income.subtract(expense));
        report.put("overdueCount", financialTransactionRepository.countByStatusAndDueDateBefore(TransactionStatus.PENDING, LocalDate.now())
                + financialTransactionRepository.countByStatusAndDueDateBefore(TransactionStatus.OVERDUE, LocalDate.now().plusDays(1)));
        return report;
    }

    @Override
    public Map<String, Object> attendanceReport(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(29);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("periodStart", start);
        report.put("periodEnd", end);
        report.put("totalCheckIns", attendanceRepository.countByCheckInBetween(start.atStartOfDay(), end.atTime(LocalTime.MAX)));
        report.put("distinctStudents", attendanceRepository.countDistinctStudentsBetween(start.atStartOfDay(), end.atTime(LocalTime.MAX)));
        return report;
    }

    @Override
    public Map<String, Object> plansReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalPlans", planRepository.count());
        Map<String, Long> activeEnrollmentsByPlan = new LinkedHashMap<>();
        enrollmentRepository.countActiveGroupedByPlan().forEach(row -> activeEnrollmentsByPlan.put(row.getPlanName(), row.getTotal()));
        report.put("activeEnrollmentsByPlan", activeEnrollmentsByPlan);
        return report;
    }

    @Override
    public Map<String, Object> productsReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalProducts", productRepository.count());
        report.put("lowStockCount", productRepository.findLowStockProducts().size());
        report.put("topSelling", stockMovementRepository.findTopSellingProducts(PageRequest.of(0, 10)).stream()
                .map(row -> Map.of("productName", row.getProductName(), "totalSold", row.getTotalSold()))
                .toList());
        return report;
    }
}
