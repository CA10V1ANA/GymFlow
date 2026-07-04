package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.request.SupplierRequest;
import com.gymflow.pro.dto.response.SupplierResponse;
import com.gymflow.pro.entity.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SupplierMapperTest {

    private SupplierMapper supplierMapper;

    @BeforeEach
    void setUp() {
        supplierMapper = new SupplierMapperImpl();
    }

    @Test
    void toEntity_shouldMapAllFields() {
        SupplierRequest request = new SupplierRequest();
        request.setName("Acme");
        request.setDocument("12345678000199");
        request.setPhone("11999999999");
        request.setEmail("acme@example.com");
        request.setAddress("Main St");

        Supplier entity = supplierMapper.toEntity(request);

        assertThat(entity.getName()).isEqualTo("Acme");
        assertThat(entity.getDocument()).isEqualTo("12345678000199");
        assertThat(entity.getAddress()).isEqualTo("Main St");
    }

    @Test
    void toEntity_shouldReturnNull_whenRequestNull() {
        assertThat(supplierMapper.toEntity(null)).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        Supplier supplier = Supplier.builder().id(id).name("Acme").document("12345678000199").build();

        SupplierResponse response = supplierMapper.toResponse(supplier);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Acme");
    }

    @Test
    void toResponse_shouldReturnNull_whenSupplierNull() {
        assertThat(supplierMapper.toResponse(null)).isNull();
    }

    @Test
    void updateEntity_shouldOnlyOverwriteNonNullFields() {
        Supplier supplier = Supplier.builder().name("Old").address("Old Address").build();
        SupplierRequest request = new SupplierRequest();
        request.setName("New");

        supplierMapper.updateEntity(request, supplier);

        assertThat(supplier.getName()).isEqualTo("New");
        assertThat(supplier.getAddress()).isEqualTo("Old Address");
    }

    @Test
    void updateEntity_shouldDoNothing_whenRequestNull() {
        Supplier supplier = Supplier.builder().name("Old").build();

        supplierMapper.updateEntity(null, supplier);

        assertThat(supplier.getName()).isEqualTo("Old");
    }
}
