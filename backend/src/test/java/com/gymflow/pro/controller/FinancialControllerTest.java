package com.gymflow.pro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymflow.pro.config.SecurityConfig;
import com.gymflow.pro.dto.request.FinancialTransactionRequest;
import com.gymflow.pro.dto.request.MarkAsPaidRequest;
import com.gymflow.pro.dto.response.FinancialTransactionResponse;
import com.gymflow.pro.entity.enums.PaymentMethod;
import com.gymflow.pro.entity.enums.TransactionCategory;
import com.gymflow.pro.entity.enums.TransactionStatus;
import com.gymflow.pro.entity.enums.TransactionType;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.security.CustomUserDetailsService;
import com.gymflow.pro.security.JwtTokenProvider;
import com.gymflow.pro.service.FinancialService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FinancialController.class)
@Import(SecurityConfig.class)
class FinancialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FinancialService financialService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private FinancialTransactionRequest validRequest() {
        FinancialTransactionRequest request = new FinancialTransactionRequest();
        request.setType(TransactionType.INCOME);
        request.setCategory(TransactionCategory.MONTHLY_FEE);
        request.setDescription("Monthly fee payment");
        request.setAmount(BigDecimal.valueOf(150));
        request.setDueDate(LocalDate.now().plusDays(5));
        return request;
    }

    private FinancialTransactionResponse sampleResponse(UUID id) {
        return FinancialTransactionResponse.builder()
                .id(id)
                .type(TransactionType.INCOME)
                .category(TransactionCategory.MONTHLY_FEE)
                .description("Monthly fee payment")
                .amount(BigDecimal.valueOf(150))
                .status(TransactionStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(5))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_withAllowedRole_returnsOk() throws Exception {
        Page<FinancialTransactionResponse> page = new PageImpl<>(List.of(sampleResponse(UUID.randomUUID())));
        when(financialService.search(any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/financial/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].description").value("Monthly fee payment"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void search_withInsufficientRole_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/financial/transactions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void findById_withAllowedRole_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(financialService.findById(eq(id))).thenReturn(sampleResponse(id));

        mockMvc.perform(get("/api/financial/transactions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(financialService.findById(eq(id))).thenThrow(ResourceNotFoundException.of("FinancialTransaction", id));

        mockMvc.perform(get("/api/financial/transactions/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cashFlow_withValidDates_returnsOk() throws Exception {
        when(financialService.cashFlow(any(), any())).thenReturn(BigDecimal.valueOf(500));

        mockMvc.perform(get("/api/financial/transactions/cash-flow")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cashFlow").value(500));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void cashFlow_withInsufficientRole_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/financial/transactions/cash-flow")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void create_withValidBody_returnsCreated() throws Exception {
        FinancialTransactionResponse response = sampleResponse(UUID.randomUUID());
        when(financialService.create(any(FinancialTransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/financial/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Monthly fee payment"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void create_withInsufficientRole_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/financial/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_withMissingRequiredField_returnsBadRequest() throws Exception {
        FinancialTransactionRequest invalid = validRequest();
        invalid.setAmount(null);

        mockMvc.perform(post("/api/financial/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.amount").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void markAsPaid_withValidBody_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        FinancialTransactionResponse response = sampleResponse(id);
        when(financialService.markAsPaid(eq(id), any(MarkAsPaidRequest.class))).thenReturn(response);

        MarkAsPaidRequest request = new MarkAsPaidRequest();
        request.setPaymentMethod(PaymentMethod.PIX);

        mockMvc.perform(post("/api/financial/transactions/{id}/pay", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void markAsPaid_withMissingPaymentMethod_returnsBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        MarkAsPaidRequest request = new MarkAsPaidRequest();

        mockMvc.perform(post("/api/financial/transactions/{id}/pay", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.paymentMethod").exists());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void cancel_withValidId_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(financialService.cancel(eq(id))).thenReturn(sampleResponse(id));

        mockMvc.perform(post("/api/financial/transactions/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void cancel_withInsufficientRole_returnsForbidden() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/api/financial/transactions/{id}/cancel", id))
                .andExpect(status().isForbidden());
    }
}
