package com.sparta.deliveryorderplatform.review.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.review.dto.request.CreateReviewRequest;
import com.sparta.deliveryorderplatform.review.dto.response.ReviewResponse;
import com.sparta.deliveryorderplatform.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * todo : 권한 처리
     * @param orderId
     * @param request
     * @return
     */
    @PostMapping("/orders/{orderId}/reviews")
    public ResponseEntity<?> createReview(@PathVariable UUID orderId,
                                          @Valid @RequestBody CreateReviewRequest request) {

        ReviewResponse response = reviewService.createReview(orderId, "tmp", request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
