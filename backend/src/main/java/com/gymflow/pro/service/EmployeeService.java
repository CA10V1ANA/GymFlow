package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.EmployeeRequest;
import com.gymflow.pro.dto.response.EmployeeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EmployeeService {

    Page<EmployeeResponse> findAll(String search, Pageable pageable);

    EmployeeResponse findById(UUID id);

    EmployeeResponse create(EmployeeRequest request);

    EmployeeResponse update(UUID id, EmployeeRequest request);

    void delete(UUID id);
}
