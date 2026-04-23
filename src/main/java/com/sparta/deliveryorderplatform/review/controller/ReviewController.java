package com.sparta.deliveryorderplatform.review.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.common.PageResponse;
import com.sparta.deliveryorderplatform.review.dto.request.CreateReviewRequest;
import com.sparta.deliveryorderplatform.review.dto.request.SearchReviewCondition;
import com.sparta.deliveryorderplatform.review.dto.request.UpdateReviewRequest;
import com.sparta.deliveryorderplatform.review.dto.response.ReviewResponse;
import com.sparta.deliveryorderplatform.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 등록 api
     * todo : 권한 처리, 주문 상태가 COMPLETED인 경우만 등록 가능
     * todo : user, store, order 찾는 로직 추가
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

    /**
     * 리뷰 목록 조회 api
     * @return
     */
    @GetMapping("/reviews")
    public ResponseEntity<?> getReviewList(@ModelAttribute SearchReviewCondition request,
                                           @PageableDefault(size = 10, page = 0) Pageable pageable) {

        Page<ReviewResponse> response = reviewService.getReviewList(request, pageable);

        return ResponseEntity.ok(PageResponse.of(response));
    }

    /**
     * 리뷰 상세 조회 api
     * @param reviewId
     * @return
     */
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<?> getReviewById(@PathVariable UUID reviewId) {

        ReviewResponse response = reviewService.getReviewById(reviewId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 리부 수정 api
     * todo : 사용자 권한 처리 추가
     * @param reviewId
     * @param request
     * @return
     */
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable UUID reviewId, @Valid @RequestBody UpdateReviewRequest request) {

        ReviewResponse response = reviewService.updateReview(reviewId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 리뷰 삭제 api
     * todo : 사용자 권한 처리 추가
     * @param reviewId
     * @return
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable UUID reviewId) {

        reviewService.deleteReview(reviewId, "tmp");

        return ResponseEntity.ok(ApiResponse.success());
    }
}
