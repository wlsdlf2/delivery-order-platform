package com.sparta.deliveryorderplatform.review.repository;

import com.sparta.deliveryorderplatform.review.dto.request.SearchReviewCondition;
import com.sparta.deliveryorderplatform.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface CustomReviewRepository {

    Page<Review> searchReviews(SearchReviewCondition condition, Pageable pageable);
}
