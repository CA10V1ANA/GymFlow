package com.gymflow.pro.scheduled;

import com.gymflow.pro.service.FinancialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Daily job that flips PENDING financial transactions whose due date has passed into OVERDUE,
 * so dashboard/report numbers stay accurate without requiring a manual refresh.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueTransactionsScheduler {

    private final FinancialService financialService;

    @Scheduled(cron = "0 5 0 * * *")
    public void markOverdueTransactions() {
        log.info("Running scheduled job to refresh overdue financial transactions");
        financialService.refreshOverdueStatuses();
    }
}
