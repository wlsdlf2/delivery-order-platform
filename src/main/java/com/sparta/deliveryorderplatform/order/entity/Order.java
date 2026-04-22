package com.sparta.deliveryorderplatform.order.entity;

import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Table(name = "p_order")
@Entity
@NoArgsConstructor
@Setter
public class Order extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id")
    private UUID id;                                // order 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false) // user 외래키
    private User user;                              // order를 한 user 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="address_id" , nullable = false) // 주소 외래키
    private Address address;                        // order 주소 식별자

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType = OrderType.ONLINE; // order 유형 , 온라인 혹은 오프라인

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;// order 상태

    @Column(nullable = false)
    private Integer totalPrice;                       // 총 주문 가격

    @Lob
    private String request; // 주문 요청 사항

}
