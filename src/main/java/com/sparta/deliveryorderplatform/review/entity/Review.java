package com.sparta.deliveryorderplatform.review.entity;

import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import com.sparta.deliveryorderplatform.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Column(unique = true, nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Min(1) @Max(5)
    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")  // null ok
    private String content;

    public void update(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }

    public static Review create(UUID orderId, UUID storeId, User user, Integer rating, String content) {
        return Review.builder()
                .orderId(orderId)
                .storeId(storeId)
                .user(user)
                .rating(rating)
                .content(content)
                .build();
    }
}
