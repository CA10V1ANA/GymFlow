package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.StockMovementRequest;
import com.gymflow.pro.dto.response.StockMovementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StockMovementService {

    Page<StockMovementResponse> findAll(Pageable pageable);

    Page<StockMovementResponse> findByProduct(UUID productId, Pageable pageable);

    StockMovementResponse register(StockMovementRequest request, String createdByEmail);
}
