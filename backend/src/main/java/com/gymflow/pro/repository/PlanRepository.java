package com.gymflow.pro.repository;

import com.gymflow.pro.entity.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan, UUID> {

    Page<Plan> findByActive(boolean active, Pageable pageable);
}
