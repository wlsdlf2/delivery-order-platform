package com.sparta.deliveryorderplatform.review;

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
import com.sparta.deliveryorderplatform.review.service.ReviewService;
import com.sparta.deliveryorderplatform.store.entity.Store;
import com.sparta.deliveryorderplatform.user.entity.User;
import com.sparta.deliveryorderplatform.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    private UUID orderId;
    private User user;
    private Order order;
    private CreateReviewRequest request;
    private Review review;
    private Store store;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        user = mock(User.class);
        order = mock(Order.class);
        request = mock(CreateReviewRequest.class);
        store = mock(Store.class);
        review = mock(Review.class);
    }

    @Nested
    class createReview {
        @Test
        void createReview_ORDER_NOT_FOUND_EXCEPTION() {

            // given
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> reviewService.createReview(orderId, user, request));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
        }

        @Test
        void createReview_ORDER_NOT_COMPLETED_EXCEPTION() {

            // given
            when(order.getStatus()).thenReturn(OrderStatus.PENDING);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> reviewService.createReview(orderId, user, request));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_ORDER_NOT_COMPLETED);
        }

        @Test
        void createReview_REVIEW_ALREADY_EXISTS_EXCEPTION() {

            // given
            when(order.getStatus()).thenReturn(OrderStatus.COMPLETED);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(reviewRepository.existsByOrder(order)).thenReturn(Boolean.TRUE);

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> reviewService.createReview(orderId, user, request));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        @Test
        void createReview_UNAUTHORIZED_EXCEPTION() {

            // given
            when(order.getStatus()).thenReturn(OrderStatus.COMPLETED);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(reviewRepository.existsByOrder(order)).thenReturn(Boolean.FALSE);

            User orderUser = mock(User.class);
            when(orderUser.getUsername()).thenReturn("other_user");
            when(order.getUser()).thenReturn(orderUser);
            when(user.getUsername()).thenReturn("current_user");

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> reviewService.createReview(orderId, user, request));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_UNAUTHORIZED);
        }

        @Test
        void createReview_SUCCESS() {
            // given
            when(order.getStatus()).thenReturn(OrderStatus.COMPLETED);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(reviewRepository.existsByOrder(order)).thenReturn(Boolean.FALSE);

            User orderUser = mock(User.class);
            when(orderUser.getUsername()).thenReturn("current_user");
            when(order.getUser()).thenReturn(orderUser);
            when(user.getUsername()).thenReturn("current_user");

            when(order.getStore()).thenReturn(store);
            when(store.getId()).thenReturn(UUID.randomUUID());
            when(reviewRepository.findAverageRatingByStoreId(any())).thenReturn(4.5);
            when(request.getRating()).thenReturn(5);

            // when
            ReviewResponse response = reviewService.createReview(orderId, user, request);

            // then
            assertThat(response).isNotNull();
            verify(reviewRepository).saveAndFlush(any(Review.class));
            verify(store).updateAverageRating(4.5);
        }
    }

    @Nested
    class getReviewList {

        @Test
        void getReviewList_size_correction() {
            // given
            Pageable pageable = PageRequest.of(0, 20);
            SearchReviewCondition condition = mock(SearchReviewCondition.class);

            when(reviewRepository.searchReviews(any(), any())).thenReturn(Page.empty());

            // when
            reviewService.getReviewList(condition, pageable);

            // then
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(reviewRepository).searchReviews(any(), pageableCaptor.capture());
            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        }

        @Test
        void getReviewList_default_sort() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SearchReviewCondition condition = mock(SearchReviewCondition.class);

            when(reviewRepository.searchReviews(any(), any())).thenReturn(Page.empty());

            // when
            reviewService.getReviewList(condition, pageable);

            // then
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(reviewRepository).searchReviews(any(), pageableCaptor.capture());
            assertThat(pageableCaptor.getValue().getSort())
                    .isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
    }

    @Nested
    class getReviewById {

        @Test
        void getReviewById_REVIEW_NOT_FOUND_EXCEPTION() {
            // given
            UUID reviewId = UUID.randomUUID();
            when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> reviewService.getReviewById(reviewId));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
        }

        @Test
        void getReviewById_SUCCESS() {
            // given
            UUID reviewId = UUID.randomUUID();
            when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));

            when(review.getOrder()).thenReturn(order);
            when(review.getStore()).thenReturn(store);
            when(review.getUser()).thenReturn(user);

            // when
            ReviewResponse response = reviewService.getReviewById(reviewId);

            // then
            assertThat(response).isNotNull();
        }
    }

    @Nested
    class updateReview {

        @Test
        void updateReview_REVIEW_NOT_FOUND_EXCEPTION() {
            // given
            UUID reviewId = UUID.randomUUID();
            when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.empty());

            UpdateReviewRequest request = mock(UpdateReviewRequest.class);
            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> reviewService.updateReview(reviewId, request, user));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
        }

        @Test
        void updateReview_UNAUTHORIZED_EXCEPTION() {
            // given
            UUID reviewId = UUID.randomUUID();
            when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));

            UpdateReviewRequest request = mock(UpdateReviewRequest.class);

            User reviewUser = mock(User.class);
            when(reviewUser.getUsername()).thenReturn("customer1");

            when(review.getUser()).thenReturn(reviewUser);
            when(user.getUsername()).thenReturn("customer2");

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> reviewService.updateReview(reviewId, request, user));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_UPDATE_FORBIDDEN);
        }

        @Test
        void updateReview_SUCCESS() {
            // given
            UUID reviewId = UUID.randomUUID();
            when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));

            UpdateReviewRequest request = mock(UpdateReviewRequest.class);

            when(review.getOrder()).thenReturn(order);

            User reviewUser = mock(User.class);
            when(reviewUser.getUsername()).thenReturn("customer1");
            when(review.getUser()).thenReturn(reviewUser);
            when(user.getUsername()).thenReturn("customer1");

            when(review.getStore()).thenReturn(store);
            when(store.getId()).thenReturn(UUID.randomUUID());
            when(reviewRepository.findAverageRatingByStoreId(any())).thenReturn(4.5);

            // when
            ReviewResponse response = reviewService.updateReview(reviewId, request, user);

            // then
            assertThat(response).isNotNull();
            verify(review).update(any(), any());
            verify(store).updateAverageRating(4.5);
        }
    }

    @Nested
    class deleteReview {

        @Test
        void deleteReview_REVIEW_NOT_FOUND_EXCEPTION() {
            // given
            UUID reviewId = UUID.randomUUID();
            when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> reviewService.deleteReview(reviewId, user, UserRole.CUSTOMER.getAuthority()));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
        }

        @Test
        void deleteReview_customer_UNAUTHORIZED_EXCEPTION() {
            // given
            UUID reviewId = UUID.randomUUID();
            when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));

            User reviewUser = mock(User.class);
            when(reviewUser.getUsername()).thenReturn("customer1");

            when(review.getUser()).thenReturn(reviewUser);
            when(user.getUsername()).thenReturn("customer2");

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> reviewService.deleteReview(reviewId, user, UserRole.CUSTOMER.getAuthority()));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_DELETE_FORBIDDEN);
        }

        @Test
        void deleteReview_customer_SUCCESS() {
            // given
            UUID reviewId = UUID.randomUUID();
            when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));

            User reviewUser = mock(User.class);
            when(reviewUser.getUsername()).thenReturn("customer1");

            when(review.getUser()).thenReturn(reviewUser);
            when(user.getUsername()).thenReturn("customer1");

            when(review.getStore()).thenReturn(store);
            when(store.getId()).thenReturn(UUID.randomUUID());
            when(reviewRepository.findAverageRatingByStoreId(any())).thenReturn(4.5);

            reviewService.deleteReview(reviewId, user, UserRole.CUSTOMER.getAuthority());

            // when & then
            verify(review).softDelete(user.getUsername());
            verify(store).updateAverageRating(4.5);
        }

        @Test
        void deleteReview_master_SUCCESS() {
            // given
            UUID reviewId = UUID.randomUUID();
            when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));

            when(review.getStore()).thenReturn(store);
            when(store.getId()).thenReturn(UUID.randomUUID());
            when(reviewRepository.findAverageRatingByStoreId(any())).thenReturn(4.5);

            reviewService.deleteReview(reviewId, user, UserRole.MASTER.getAuthority());

            // when & then
            verify(review).softDelete(user.getUsername());
            verify(store).updateAverageRating(4.5);
        }
    }
}
