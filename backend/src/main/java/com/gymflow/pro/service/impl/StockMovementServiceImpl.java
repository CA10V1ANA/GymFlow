package com.gymflow.pro.service.impl;

import com.gymflow.pro.dto.request.StockMovementRequest;
import com.gymflow.pro.dto.response.StockMovementResponse;
import com.gymflow.pro.entity.Product;
import com.gymflow.pro.entity.StockMovement;
import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.User;
import com.gymflow.pro.entity.enums.StockMovementType;
import com.gymflow.pro.exception.BusinessException;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.StockMovementMapper;
import com.gymflow.pro.repository.ProductRepository;
import com.gymflow.pro.repository.StockMovementRepository;
import com.gymflow.pro.repository.StudentRepository;
import com.gymflow.pro.repository.UserRepository;
import com.gymflow.pro.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StockMovementMapper stockMovementMapper;

    @Override
    public Page<StockMovementResponse> findAll(Pageable pageable) {
        return stockMovementRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(stockMovementMapper::toResponse);
    }

    @Override
    public Page<StockMovementResponse> findByProduct(UUID productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", productId));
        return stockMovementRepository.findByProductOrderByCreatedAtDesc(product, pageable)
                .map(stockMovementMapper::toResponse);
    }

    @Override
    @Transactional
    public StockMovementResponse register(StockMovementRequest request, String createdByEmail) {
        // Lock the product row to keep the stock quantity update atomic under concurrent movements.
        Product product = productRepository.findByIdForUpdate(request.getProductId())
                .orElseThrow(() -> ResourceNotFoundException.of("Product", request.getProductId()));

        int delta = request.getType() == StockMovementType.ENTRY ? request.getQuantity() : -request.getQuantity();
        int newQuantity = product.getStockQuantity() + delta;
        if (newQuantity < 0) {
            throw new BusinessException("Insufficient stock for product: " + product.getName());
        }
        product.setStockQuantity(newQuantity);
        productRepository.save(product);

        Student student = null;
        if (request.getStudentId() != null) {
            student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Student", request.getStudentId()));
        }

        User createdBy = createdByEmail != null ? userRepository.findByEmail(createdByEmail).orElse(null) : null;

        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(request.getType())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .reason(request.getReason())
                .student(student)
                .createdBy(createdBy)
                .build();

        return stockMovementMapper.toResponse(stockMovementRepository.save(movement));
    }
}
