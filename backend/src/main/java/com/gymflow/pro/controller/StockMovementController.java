package com.gymflow.pro.controller;

import com.gymflow.pro.dto.request.StockMovementRequest;
import com.gymflow.pro.dto.response.StockMovementResponse;
import com.gymflow.pro.service.StockMovementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
@Tag(name = "Stock Movements", description = "Product inventory entries and exits")
@PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @GetMapping
    public ResponseEntity<Page<StockMovementResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(stockMovementService.findAll(pageable));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<StockMovementResponse>> findByProduct(@PathVariable UUID productId, Pageable pageable) {
        return ResponseEntity.ok(stockMovementService.findByProduct(productId, pageable));
    }

    @PostMapping
    public ResponseEntity<StockMovementResponse> register(@Valid @RequestBody StockMovementRequest request,
                                                            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockMovementService.register(request, authentication.getName()));
    }
}
