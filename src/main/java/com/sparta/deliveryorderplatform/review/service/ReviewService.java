package com.sparta.deliveryorderplatform.review.service;

import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.review.dto.request.CreateReviewRequest;
import com.sparta.deliveryorderplatform.review.dto.request.SearchReviewCondition;
import com.sparta.deliveryorderplatform.review.dto.request.UpdateReviewRequest;
import com.sparta.deliveryorderplatform.review.dto.response.ReviewResponse;
import com.sparta.deliveryorderplatform.review.entity.Review;
import com.sparta.deliveryorderplatform.review.repository.ReviewRepository;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import com.sparta.deliveryorderplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse createReview(UUID orderId, String username,
                                       CreateReviewRequest request) {

        // Order 찾는 로직(1주문 1리뷰), 주문 상태가 Completed인 경우에만 ㄱㄴ

        // User 찾는 로직
        User user = userRepository.findById(username).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        // store 찾는 로직

        Review review = Review.create(orderId, request.getStoreId(),
                user, request.getRating(), request.getContent());

        return ReviewResponse.from(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewList(SearchReviewCondition request, Pageable pageable) {

        int size = pageable.getPageSize();
        if (size != 10 && size != 30 && size != 50)
            size = 10;

        Sort sort = pageable.getSort().isSorted() ?
                pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable validatedPageable = PageRequest.of(pageable.getPageNumber(), size,
                sort);

        Page<Review> reviews = reviewRepository.searchReviews(request, validatedPageable);

        return reviews.map(ReviewResponse::from);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(UUID reviewId) {

        return ReviewResponse.from(this.findReviewById(reviewId));
    }

    @Transactional
    public ReviewResponse updateReview(UUID reviewId, UpdateReviewRequest request, String username) {

        Review review = this.findReviewById(reviewId);

        // 본인 리뷰만 수정 가능하도록
        if (!review.getUser().getUsername().equals(username)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        review.update(request.getRating(), request.getContent());

        return ReviewResponse.from(review);
    }

    @Transactional
    public void deleteReview(UUID reviewId, String username, String role) {

        Review review = this.findReviewById(reviewId);

        // 본인 리뷰만 삭제 가능(manager&master는 모두 허용)
        if (role.equals(UserRole.CUSTOMER.getAuthority()) && !review.getUser().getUsername().equals(username)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        review.softDelete(username);
    }

    private Review findReviewById(UUID id) {
        return reviewRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new CustomException(ErrorCode.REVIEW_NOT_FOUND)
        );
    }
}
