package com.sparta.deliveryorderplatform.review.entity;

import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.order.entity.Order;
import com.sparta.deliveryorderplatform.store.entity.Store;
import com.sparta.deliveryorderplatform.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_review")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Review extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")  // null ok
    private String content;

    public void update(Integer rating, String content) {

        if (rating == null || rating < 1 || rating > 5) {
            throw new CustomException(ErrorCode.INVALID_RATING);
        }

        this.rating = rating;
        this.content = content;
    }

    public static Review create(Order order, Integer rating, String content) {

        if (rating == null || rating < 1 || rating > 5) {
            throw new CustomException(ErrorCode.INVALID_RATING);
        }

        return Review.builder()
                .order(order)
                .store(order.getStore())
                .user(order.getUser())
                .rating(rating)
                .content(content)
                .build();
    }
}
