package com.sparta.deliveryorderplatform.review.controller;

import com.sparta.deliveryorderplatform.global.common.ApiResponse;
import com.sparta.deliveryorderplatform.global.common.PageResponse;
import com.sparta.deliveryorderplatform.review.dto.request.CreateReviewRequest;
import com.sparta.deliveryorderplatform.review.dto.request.SearchReviewCondition;
import com.sparta.deliveryorderplatform.review.dto.request.UpdateReviewRequest;
import com.sparta.deliveryorderplatform.review.dto.response.ReviewResponse;
import com.sparta.deliveryorderplatform.review.service.ReviewService;
import com.sparta.deliveryorderplatform.user.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 등록 api
     * @param orderId
     * @param request
     * @param userDetails
     * @return
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/orders/{orderId}/reviews")
    public ResponseEntity<?> createReview(@PathVariable UUID orderId,
                                          @Valid @RequestBody CreateReviewRequest request,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ReviewResponse response = reviewService.createReview(orderId, userDetails.getUser(), request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 리뷰 목록 조회 api
     * @param request
     * @param pageable
     * @return
     */
    @GetMapping("/reviews")
    public ResponseEntity<?> getReviewList(@ModelAttribute SearchReviewCondition request,
                                           @PageableDefault(size = 10, page = 0) Pageable pageable) {

        Page<ReviewResponse> response = reviewService.getReviewList(request, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(response)));
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
     * 리뷰 수정 api
     * @param reviewId
     * @param request
     * @param userDetails
     * @return
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable UUID reviewId,
                                          @Valid @RequestBody UpdateReviewRequest request,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ReviewResponse response = reviewService.updateReview(reviewId, request, userDetails.getUser());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 리뷰 삭제 api
     * @param reviewId
     * @param userDetails
     * @return
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable UUID reviewId,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {

        reviewService.deleteReview(
                reviewId,
                userDetails.getUser(),
                userDetails.getAuthorities().iterator().next().getAuthority());

        return ResponseEntity.ok(ApiResponse.success());
    }
}
