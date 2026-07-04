package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.SupplierRequest;
import com.gymflow.pro.dto.response.SupplierResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SupplierService {

    Page<SupplierResponse> findAll(Pageable pageable);

    SupplierResponse findById(UUID id);

    SupplierResponse create(SupplierRequest request);

    SupplierResponse update(UUID id, SupplierRequest request);

    void delete(UUID id);
}
