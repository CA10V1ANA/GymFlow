package com.gymflow.pro.scheduled;

import com.gymflow.pro.service.FinancialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OverdueTransactionsSchedulerTest {

    @Mock
    private FinancialService financialService;

    private OverdueTransactionsScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new OverdueTransactionsScheduler(financialService);
    }

    @Test
    void markOverdueTransactions_shouldDelegateToFinancialService() {
        scheduler.markOverdueTransactions();

        verify(financialService).refreshOverdueStatuses();
    }
}
