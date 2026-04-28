package com.sparta.deliveryorderplatform.review.repository;

import com.sparta.deliveryorderplatform.order.entity.Order;
import com.sparta.deliveryorderplatform.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID>, CustomReviewRepository {

    Optional<Review> findByIdAndDeletedAtIsNull(UUID reviewId);

    boolean existsByOrder(Order order);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.store.id = :storeId AND r.deletedAt IS NULL")
    Double findAverageRatingByStoreId(@Param("storeId") UUID storeId);
}
