package com.gymflow.pro.service;

import com.gymflow.pro.entity.Product;
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
import com.gymflow.pro.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private FinancialTransactionRepository financialTransactionRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(studentRepository, enrollmentRepository, planRepository,
                productRepository, stockMovementRepository, financialTransactionRepository, attendanceRepository);
    }

    @Test
    void studentsReport_shouldAggregateCountsByStatus() {
        when(studentRepository.count()).thenReturn(100L);
        when(studentRepository.countByStatus(StudentStatus.ACTIVE)).thenReturn(70L);
        when(studentRepository.countByStatus(StudentStatus.INACTIVE)).thenReturn(20L);
        when(studentRepository.countByStatus(StudentStatus.PENDING)).thenReturn(10L);

        Map<String, Object> result = reportService.studentsReport();

        assertThat(result.get("total")).isEqualTo(100L);
        assertThat(result.get("active")).isEqualTo(70L);
        assertThat(result.get("inactive")).isEqualTo(20L);
        assertThat(result.get("pending")).isEqualTo(10L);
    }

    @Test
    void financialReport_shouldComputeNetResult_withDefaultPeriod_whenDatesNull() {
        when(financialTransactionRepository.sumPaidByTypeAndPeriod(eq(TransactionType.INCOME), any(), any()))
                .thenReturn(BigDecimal.valueOf(1000));
        when(financialTransactionRepository.sumPaidByTypeAndPeriod(eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(BigDecimal.valueOf(400));
        when(financialTransactionRepository.countByStatusAndDueDateBefore(eq(TransactionStatus.PENDING), any()))
                .thenReturn(2L);
        when(financialTransactionRepository.countByStatusAndDueDateBefore(eq(TransactionStatus.OVERDUE), any()))
                .thenReturn(3L);

        Map<String, Object> result = reportService.financialReport(null, null);

        assertThat(result.get("totalIncome")).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(result.get("totalExpense")).isEqualTo(BigDecimal.valueOf(400));
        assertThat(result.get("netResult")).isEqualTo(BigDecimal.valueOf(600));
        assertThat(result.get("overdueCount")).isEqualTo(5L);
        assertThat(result.get("periodStart")).isEqualTo(LocalDate.now().withDayOfMonth(1));
        assertThat(result.get("periodEnd")).isEqualTo(LocalDate.now());
    }

    @Test
    void financialReport_shouldUseProvidedPeriod_whenDatesGiven() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);
        when(financialTransactionRepository.sumPaidByTypeAndPeriod(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(financialTransactionRepository.countByStatusAndDueDateBefore(any(), any())).thenReturn(0L);

        Map<String, Object> result = reportService.financialReport(start, end);

        assertThat(result.get("periodStart")).isEqualTo(start);
        assertThat(result.get("periodEnd")).isEqualTo(end);
    }

    @Test
    void attendanceReport_shouldAggregateCheckInsAndDistinctStudents() {
        when(attendanceRepository.countByCheckInBetween(any(), any())).thenReturn(50L);
        when(attendanceRepository.countDistinctStudentsBetween(any(), any())).thenReturn(15L);

        Map<String, Object> result = reportService.attendanceReport(null, null);

        assertThat(result.get("totalCheckIns")).isEqualTo(50L);
        assertThat(result.get("distinctStudents")).isEqualTo(15L);
        assertThat(result.get("periodStart")).isEqualTo(LocalDate.now().minusDays(29));
        assertThat(result.get("periodEnd")).isEqualTo(LocalDate.now());
    }

    @Test
    void plansReport_shouldAggregateActiveEnrollmentsByPlan() {
        EnrollmentRepository.PlanCount planCount = new EnrollmentRepository.PlanCount() {
            @Override
            public String getPlanName() {
                return "Gold";
            }

            @Override
            public Long getTotal() {
                return 25L;
            }
        };
        when(planRepository.count()).thenReturn(3L);
        when(enrollmentRepository.countActiveGroupedByPlan()).thenReturn(List.of(planCount));

        Map<String, Object> result = reportService.plansReport();

        assertThat(result.get("totalPlans")).isEqualTo(3L);
        @SuppressWarnings("unchecked")
        Map<String, Long> byPlan = (Map<String, Long>) result.get("activeEnrollmentsByPlan");
        assertThat(byPlan).containsEntry("Gold", 25L);
    }

    @Test
    void productsReport_shouldAggregateLowStockAndTopSelling() {
        UUID productId = UUID.randomUUID();
        Product lowStockProduct = Product.builder().id(productId).name("Whey").build();
        StockMovementRepository.TopProduct topProduct = new StockMovementRepository.TopProduct() {
            @Override
            public UUID getProductId() {
                return productId;
            }

            @Override
            public String getProductName() {
                return "Whey";
            }

            @Override
            public Long getTotalSold() {
                return 40L;
            }
        };

        when(productRepository.count()).thenReturn(10L);
        when(productRepository.findLowStockProducts()).thenReturn(List.of(lowStockProduct));
        when(stockMovementRepository.findTopSellingProducts(any())).thenReturn(List.of(topProduct));

        Map<String, Object> result = reportService.productsReport();

        assertThat(result.get("totalProducts")).isEqualTo(10L);
        assertThat(result.get("lowStockCount")).isEqualTo(1);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topSelling = (List<Map<String, Object>>) result.get("topSelling");
        assertThat(topSelling).hasSize(1);
        assertThat(topSelling.get(0)).containsEntry("productName", "Whey").containsEntry("totalSold", 40L);
    }
}
