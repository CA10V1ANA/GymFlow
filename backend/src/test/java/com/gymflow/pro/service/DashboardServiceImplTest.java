package com.gymflow.pro.service;

import com.gymflow.pro.dto.response.DashboardSummaryResponse;
import com.gymflow.pro.entity.enums.StudentStatus;
import com.gymflow.pro.entity.enums.TransactionStatus;
import com.gymflow.pro.entity.enums.TransactionType;
import com.gymflow.pro.repository.AttendanceRepository;
import com.gymflow.pro.repository.EnrollmentRepository;
import com.gymflow.pro.repository.FinancialTransactionRepository;
import com.gymflow.pro.repository.StockMovementRepository;
import com.gymflow.pro.repository.StudentRepository;
import com.gymflow.pro.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private FinancialTransactionRepository financialTransactionRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        when(enrollmentRepository.countActiveGroupedByPlan()).thenReturn(List.of());
        when(stockMovementRepository.findTopSellingProducts(any())).thenReturn(List.of());
        when(financialTransactionRepository.monthlyIncomeSince(any())).thenReturn(List.of());
    }

    @Test
    void summary_shouldAggregateStudentAndAttendanceCounts() {
        when(studentRepository.count()).thenReturn(100L);
        when(studentRepository.countByCreatedAtGreaterThanEqual(any())).thenReturn(5L);
        when(studentRepository.countByStatus(StudentStatus.ACTIVE)).thenReturn(80L);
        when(studentRepository.countByStatus(StudentStatus.INACTIVE)).thenReturn(20L);
        when(financialTransactionRepository.countByStatusAndDueDateBefore(eq(TransactionStatus.PENDING), any()))
                .thenReturn(3L);
        when(financialTransactionRepository.countByStatusAndDueDateBefore(eq(TransactionStatus.OVERDUE), any()))
                .thenReturn(2L);
        when(financialTransactionRepository.sumPaidByTypeAndPeriod(eq(TransactionType.INCOME), any(), any()))
                .thenReturn(BigDecimal.valueOf(1000));
        when(attendanceRepository.countByCheckInBetween(any(), any())).thenReturn(10L);

        DashboardSummaryResponse result = dashboardService.summary();

        assertThat(result.getTotalStudents()).isEqualTo(100L);
        assertThat(result.getNewStudentsThisMonth()).isEqualTo(5L);
        assertThat(result.getActiveStudents()).isEqualTo(80L);
        assertThat(result.getInactiveStudents()).isEqualTo(20L);
        assertThat(result.getOverdueTransactions()).isEqualTo(5L);
        assertThat(result.getMonthlyRevenue()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(result.getAnnualRevenue()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(result.getDailyAttendance()).isEqualTo(10L);
        assertThat(result.getWeeklyAttendance()).isEqualTo(10L);
    }

    @Test
    void summary_shouldPopulateStudentsByPlanFromEnrollmentRepository() {
        stubDefaultsForNonPlanFields();

        EnrollmentRepository.PlanCount planCountA = mockPlanCount("Gold", 10L);
        EnrollmentRepository.PlanCount planCountB = mockPlanCount("Silver", 5L);
        when(enrollmentRepository.countActiveGroupedByPlan()).thenReturn(List.of(planCountA, planCountB));

        DashboardSummaryResponse result = dashboardService.summary();

        assertThat(result.getStudentsByPlan()).containsExactly(
                java.util.Map.entry("Gold", 10L),
                java.util.Map.entry("Silver", 5L));
    }

    @Test
    void summary_shouldPopulateTopProductsFromStockMovementRepository() {
        stubDefaultsForNonPlanFields();

        StockMovementRepository.TopProduct topProduct = mockTopProduct("Protein Bar", 42L);
        when(stockMovementRepository.findTopSellingProducts(any())).thenReturn(List.of(topProduct));

        DashboardSummaryResponse result = dashboardService.summary();

        assertThat(result.getTopProducts()).hasSize(1);
        assertThat(result.getTopProducts().get(0).getProductName()).isEqualTo("Protein Bar");
        assertThat(result.getTopProducts().get(0).getTotalSold()).isEqualTo(42L);
    }

    @Test
    void summary_shouldBuildTwelveMonthRevenueSeriesWithZeroFilledMonths() {
        stubDefaultsForNonPlanFields();

        DashboardSummaryResponse result = dashboardService.summary();

        assertThat(result.getRevenueLastTwelveMonths()).hasSize(12);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String currentMonthKey = YearMonth.now().format(formatter);
        assertThat(result.getRevenueLastTwelveMonths().get(11).getMonth()).isEqualTo(currentMonthKey);
        assertThat(result.getRevenueLastTwelveMonths())
                .allSatisfy(point -> assertThat(point.getRevenue()).isEqualByComparingTo(BigDecimal.ZERO));
    }

    @Test
    void summary_shouldMergeMonthlyIncomeIntoRevenueSeries() {
        stubDefaultsForNonPlanFields();

        YearMonth currentMonth = YearMonth.now();
        FinancialTransactionRepository.MonthlyTotal monthlyTotal = mockMonthlyTotal(
                Timestamp.valueOf(currentMonth.atDay(1).atStartOfDay()), BigDecimal.valueOf(2500));
        when(financialTransactionRepository.monthlyIncomeSince(any())).thenReturn(List.of(monthlyTotal));

        DashboardSummaryResponse result = dashboardService.summary();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String currentMonthKey = currentMonth.format(formatter);
        assertThat(result.getRevenueLastTwelveMonths())
                .filteredOn(point -> point.getMonth().equals(currentMonthKey))
                .extracting(DashboardSummaryResponse.MonthlyRevenuePoint::getRevenue)
                .containsExactly(BigDecimal.valueOf(2500));
    }

    private void stubDefaultsForNonPlanFields() {
        when(studentRepository.count()).thenReturn(0L);
        when(studentRepository.countByCreatedAtGreaterThanEqual(any())).thenReturn(0L);
        when(studentRepository.countByStatus(any())).thenReturn(0L);
        when(financialTransactionRepository.countByStatusAndDueDateBefore(any(), any())).thenReturn(0L);
        when(financialTransactionRepository.sumPaidByTypeAndPeriod(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(attendanceRepository.countByCheckInBetween(any(), any())).thenReturn(0L);
    }

    private EnrollmentRepository.PlanCount mockPlanCount(String planName, Long total) {
        EnrollmentRepository.PlanCount planCount = org.mockito.Mockito.mock(EnrollmentRepository.PlanCount.class);
        when(planCount.getPlanName()).thenReturn(planName);
        when(planCount.getTotal()).thenReturn(total);
        return planCount;
    }

    private StockMovementRepository.TopProduct mockTopProduct(String productName, Long totalSold) {
        StockMovementRepository.TopProduct topProduct = org.mockito.Mockito.mock(StockMovementRepository.TopProduct.class);
        when(topProduct.getProductName()).thenReturn(productName);
        when(topProduct.getTotalSold()).thenReturn(totalSold);
        return topProduct;
    }

    private FinancialTransactionRepository.MonthlyTotal mockMonthlyTotal(Timestamp month, BigDecimal total) {
        FinancialTransactionRepository.MonthlyTotal monthlyTotal =
                org.mockito.Mockito.mock(FinancialTransactionRepository.MonthlyTotal.class);
        when(monthlyTotal.getMonth()).thenReturn(month);
        when(monthlyTotal.getTotal()).thenReturn(total);
        return monthlyTotal;
    }
}
