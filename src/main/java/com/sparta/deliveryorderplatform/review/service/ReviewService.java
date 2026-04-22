package com.sparta.deliveryorderplatform.review.service;

import com.sparta.deliveryorderplatform.review.dto.request.CreateReviewRequest;
import com.sparta.deliveryorderplatform.review.dto.response.ReviewResponse;
import com.sparta.deliveryorderplatform.review.entity.Review;
import com.sparta.deliveryorderplatform.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewResponse createReview(UUID orderId, String username, CreateReviewRequest request) {

        // Order 찾는 로직

        // User 찾는 로직

        // store 찾는 로직

        Review review = Review.create(orderId, request.getStoreId(),
                username, request.getRating(), request.getContent());

        return ReviewResponse.from(reviewRepository.save(review));
    }


}
