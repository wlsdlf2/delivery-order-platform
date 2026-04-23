package com.sparta.deliveryorderplatform.order.practice;

import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import com.sparta.deliveryorderplatform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "p_store")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseAuditEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "store_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(name = "average_rating", precision = 2, scale = 1)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;
}
