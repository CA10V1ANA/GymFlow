package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.SupplierRequest;
import com.gymflow.pro.dto.response.SupplierResponse;
import com.gymflow.pro.entity.Supplier;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.SupplierMapper;
import com.gymflow.pro.repository.SupplierRepository;
import com.gymflow.pro.service.impl.SupplierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    private SupplierServiceImpl supplierService;

    private Supplier supplier;
    private UUID supplierId;

    @BeforeEach
    void setUp() {
        supplierService = new SupplierServiceImpl(supplierRepository, supplierMapper);
        supplierId = UUID.randomUUID();
        supplier = Supplier.builder().id(supplierId).name("Acme Supplies").build();
    }

    private SupplierRequest buildRequest() {
        SupplierRequest request = new SupplierRequest();
        request.setName("Acme Supplies");
        request.setDocument("12345678000199");
        request.setPhone("11999999999");
        request.setEmail("contact@acme.com");
        request.setAddress("Main St 123");
        return request;
    }

    @Test
    void findAll_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        SupplierResponse response = SupplierResponse.builder().id(supplierId).name("Acme Supplies").build();
        when(supplierRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(supplier)));
        when(supplierMapper.toResponse(supplier)).thenReturn(response);

        Page<SupplierResponse> result = supplierService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void findById_shouldReturnSupplier_whenExists() {
        SupplierResponse response = SupplierResponse.builder().id(supplierId).name("Acme Supplies").build();
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierMapper.toResponse(supplier)).thenReturn(response);

        SupplierResponse result = supplierService.findById(supplierId);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void findById_shouldThrowResourceNotFound_whenMissing() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.findById(supplierId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldSaveAndReturnMappedResponse() {
        SupplierRequest request = buildRequest();
        Supplier mappedEntity = Supplier.builder().name("Acme Supplies").build();
        SupplierResponse response = SupplierResponse.builder().id(supplierId).name("Acme Supplies").build();

        when(supplierMapper.toEntity(request)).thenReturn(mappedEntity);
        when(supplierRepository.save(mappedEntity)).thenReturn(supplier);
        when(supplierMapper.toResponse(supplier)).thenReturn(response);

        SupplierResponse result = supplierService.create(request);

        assertThat(result).isEqualTo(response);
        verify(supplierRepository).save(mappedEntity);
    }

    @Test
    void update_shouldUpdateAndReturnMappedResponse() {
        SupplierRequest request = buildRequest();
        SupplierResponse response = SupplierResponse.builder().id(supplierId).name("Acme Supplies").build();

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(supplier)).thenReturn(supplier);
        when(supplierMapper.toResponse(supplier)).thenReturn(response);

        SupplierResponse result = supplierService.update(supplierId, request);

        assertThat(result).isEqualTo(response);
        verify(supplierMapper).updateEntity(request, supplier);
        verify(supplierRepository).save(supplier);
    }

    @Test
    void update_shouldThrowResourceNotFound_whenMissing() {
        SupplierRequest request = buildRequest();
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.update(supplierId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(supplierRepository, never()).save(any());
    }

    @Test
    void delete_shouldRemoveSupplier_whenExists() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));

        supplierService.delete(supplierId);

        verify(supplierRepository).delete(supplier);
    }

    @Test
    void delete_shouldThrowResourceNotFound_whenMissing() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.delete(supplierId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(supplierRepository, never()).delete(any());
    }
}
