package com.gymflow.pro.repository;

import com.gymflow.pro.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {

    boolean existsByNameIgnoreCase(String name);
}
