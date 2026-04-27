package com.sparta.deliveryorderplatform.review.dto.response;

import com.sparta.deliveryorderplatform.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ReviewResponse {

    private UUID reviewId;

    private UUID orderId;

    private UUID storeId;

    private String username;

    private Integer rating;

    private String content;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .orderId(review.getOrder().getId())
                .storeId(review.getStore().getId())
                .username(review.getUser().getUsername())
                .rating(review.getRating())
                .content(review.getContent())
                .build();
    }
}
