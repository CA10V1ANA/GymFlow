package com.gymflow.pro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private long totalStudents;
    private long newStudentsThisMonth;
    private long activeStudents;
    private long inactiveStudents;
    private long overdueTransactions;
    private BigDecimal monthlyRevenue;
    private BigDecimal annualRevenue;
    private long dailyAttendance;
    private long weeklyAttendance;
    private Map<String, Long> studentsByPlan;
    private List<TopProductResponse> topProducts;
    private List<MonthlyRevenuePoint> revenueLastTwelveMonths;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProductResponse {
        private String productName;
        private long totalSold;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenuePoint {
        private String month; // yyyy-MM
        private BigDecimal revenue;
    }
}
