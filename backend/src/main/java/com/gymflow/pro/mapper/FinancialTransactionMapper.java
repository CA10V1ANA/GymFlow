package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.FinancialTransactionResponse;
import com.gymflow.pro.entity.FinancialTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = StudentMapper.class)
public interface FinancialTransactionMapper {

    @Mapping(target = "student", source = "student")
    @Mapping(target = "enrollmentId", source = "enrollment.id")
    @Mapping(target = "netAmount", expression = "java(transaction.getAmount().subtract(transaction.getDiscount()).add(transaction.getPenalty()))")
    FinancialTransactionResponse toResponse(FinancialTransaction transaction);
}
