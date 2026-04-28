package com.sparta.deliveryorderplatform.order.entity;

import java.util.UUID;

import com.sparta.deliveryorderplatform.address.entity.Address;
import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import com.sparta.deliveryorderplatform.order.dto.OrderRequestDto;
import com.sparta.deliveryorderplatform.store.entity.Store;
import com.sparta.deliveryorderplatform.user.entity.User;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Where;


@Getter
@Table(name = "p_order")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at is NULL") // 삭제 처리된 데이터는 조회되지 않도록 설정.
public class Order extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id")
    private UUID id;                                // order 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false) // user 외래키
    private User user;                              // order를 한 user 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id",nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="address_id" , nullable = false) // 주소 외래키
    private Address address;                        // order 주소 식별자

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    @Builder.Default
    private OrderType orderType = OrderType.ONLINE; // order 유형 , 온라인 혹은 오프라인

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;// order 상태

    @Column(nullable = false)
    private Integer totalPrice;                       // 총 주문 가격

    @Lob
    private String request; // 주문 요청 사항

    //주문 상태 변경.
    public void statusUpdate(String status){
        this.status = OrderStatus.valueOf(status);
    }

    //주문 요청 사항 변경
    public void update(OrderRequestDto req, Store store, Address address) {
        this.store = store;
        this.address = address;
        this.orderType = req.getOrderType();
        this.request = req.getRequest();
    }

    // 주문 생성.
    public static Order createOrder(User user, Store store,  Address address, OrderType orderType,Integer totalPrice, String request) {
        return Order.builder()
                    .user(user)
                    .store(store)
                    .address(address)
                    .orderType(orderType)
                    .totalPrice(totalPrice)
                    .request(request)
                    .build();
    }

}
