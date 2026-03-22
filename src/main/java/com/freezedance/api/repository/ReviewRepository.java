package com.freezedance.api.repository;

import com.freezedance.api.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductIdAndIsApprovedTrue(Long productId, Pageable pageable);

    Page<Review> findByProductId(Long productId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
    Double averageRatingByProductId(Long productId);

    long countByProductIdAndIsApprovedTrue(Long productId);

    boolean existsByProductIdAndUserId(Long productId, Long userId);
}
