package com.sparta.deliveryorderplatform.review.service;

import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.order.entity.Order;
import com.sparta.deliveryorderplatform.order.entity.OrderStatus;
import com.sparta.deliveryorderplatform.order.repository.OrderRepository;
import com.sparta.deliveryorderplatform.review.dto.request.CreateReviewRequest;
import com.sparta.deliveryorderplatform.review.dto.request.SearchReviewCondition;
import com.sparta.deliveryorderplatform.review.dto.request.UpdateReviewRequest;
import com.sparta.deliveryorderplatform.review.dto.response.ReviewResponse;
import com.sparta.deliveryorderplatform.review.entity.Review;
import com.sparta.deliveryorderplatform.review.repository.ReviewRepository;
import com.sparta.deliveryorderplatform.store.entity.Store;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
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
    private final OrderRepository orderRepository;

    @Transactional
    public ReviewResponse createReview(UUID orderId, User user,
                                       CreateReviewRequest request) {

        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new CustomException(ErrorCode.ORDER_NOT_FOUND)
        );

        // 주문 상태 completed 여부 검증
        if (!order.getStatus().equals(OrderStatus.COMPLETED)) {
            throw new CustomException(ErrorCode.REVIEW_ORDER_NOT_COMPLETED);
        }

        // 1주문 1리뷰 검증
        if (reviewRepository.existsByOrder(order)) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // 본인 주문 건에만 리뷰 등록 가능
        if (!order.getUser().getUsername().equals(user.getUsername())) {
            throw new CustomException(ErrorCode.REVIEW_UNAUTHORIZED);
        }

        Review review = Review.create(order, request.getRating(), request.getContent());

        // db에 즉시 반영
        reviewRepository.saveAndFlush(review);

        // 가게 리뷰 평점 계산
        updateStoreAverageRating(review.getStore());

        return ReviewResponse.from(review);
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
    public ReviewResponse updateReview(UUID reviewId, UpdateReviewRequest request, User user) {

        Review review = this.findReviewById(reviewId);

        // 본인 리뷰만 수정 가능하도록
        if (!review.getUser().getUsername().equals(user.getUsername())) {
            throw new CustomException(ErrorCode.REVIEW_UPDATE_FORBIDDEN);
        }

        // 리뷰 업데이트
        review.update(request.getRating(), request.getContent());
        reviewRepository.flush();

        // 가게 리뷰 평점 계산
        updateStoreAverageRating(review.getStore());

        return ReviewResponse.from(review);
    }

    @Transactional
    public void deleteReview(UUID reviewId, User user, String role) {

        Review review = this.findReviewById(reviewId);

        // customer인 경우 본인 리뷰인지 검증
        if (role.equals(UserRole.CUSTOMER.getAuthority()) &&
                !review.getUser().getUsername().equals(user.getUsername())) {
            throw new CustomException(ErrorCode.REVIEW_DELETE_FORBIDDEN);
        }

        // 삭제
        review.softDelete(user.getUsername());
        reviewRepository.flush();

        // 가게 리뷰 평점 계산
        updateStoreAverageRating(review.getStore());
    }

    private Review findReviewById(UUID id) {
        return reviewRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new CustomException(ErrorCode.REVIEW_NOT_FOUND)
        );
    }

    private void updateStoreAverageRating(Store store) {
        Double newAverageRating = reviewRepository.findAverageRatingByStoreId(store.getId());
        store.updateAverageRating(newAverageRating);
    }
}
