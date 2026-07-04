package com.gymflow.pro.service.impl;

import com.gymflow.pro.dto.request.SupplierRequest;
import com.gymflow.pro.dto.response.SupplierResponse;
import com.gymflow.pro.entity.Supplier;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.SupplierMapper;
import com.gymflow.pro.repository.SupplierRepository;
import com.gymflow.pro.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    public Page<SupplierResponse> findAll(Pageable pageable) {
        return supplierRepository.findAll(pageable).map(supplierMapper::toResponse);
    }

    @Override
    public SupplierResponse findById(UUID id) {
        return supplierMapper.toResponse(getSupplierOrThrow(id));
    }

    @Override
    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        return supplierMapper.toResponse(supplierRepository.save(supplierMapper.toEntity(request)));
    }

    @Override
    @Transactional
    public SupplierResponse update(UUID id, SupplierRequest request) {
        Supplier supplier = getSupplierOrThrow(id);
        supplierMapper.updateEntity(request, supplier);
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        supplierRepository.delete(getSupplierOrThrow(id));
    }

    private Supplier getSupplierOrThrow(UUID id) {
        return supplierRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Supplier", id));
    }
}
