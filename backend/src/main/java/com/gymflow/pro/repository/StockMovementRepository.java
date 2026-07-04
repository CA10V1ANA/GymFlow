package com.gymflow.pro.repository;

import com.gymflow.pro.entity.Product;
import com.gymflow.pro.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    Page<StockMovement> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<StockMovement> findByProductOrderByCreatedAtDesc(Product product, Pageable pageable);

    @Query("select sm.product.id as productId, sm.product.name as productName, sum(sm.quantity) as totalSold " +
            "from StockMovement sm where sm.type = 'EXIT' group by sm.product.id, sm.product.name " +
            "order by sum(sm.quantity) desc")
    List<TopProduct> findTopSellingProducts(org.springframework.data.domain.Pageable pageable);

    interface TopProduct {
        UUID getProductId();
        String getProductName();
        Long getTotalSold();
    }
}
