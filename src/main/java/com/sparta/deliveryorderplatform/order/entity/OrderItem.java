package com.sparta.deliveryorderplatform.order.entity;

import com.sparta.deliveryorderplatform.menu.entity.Menu;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "p_order_item")
@EntityListeners(AuditingEntityListener.class) //JPA가 자동으로 시간을 기록해주는 장치
public class OrderItem  {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_item_id")
    private UUID id;                                // 주문 메뉴 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",nullable = false)
    private Order order;                            // 주문 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;                              // 메뉴 식별자

    @Column(nullable = false)
    private Integer quantity;                       // 메뉴 수량 , check > 0 제약 조건 추가 필요

    @Column(name = "unit_price",nullable = false)
    private Integer unitPrice;                      // 주문 당시 단가

    @CreatedDate
    @Column(name = "created_at", updatable = false ,nullable = false)
    private LocalDateTime createdAt;                // 생성일자

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;                       // 생성한 사람.

    public static OrderItem createOrderItem(Order  order, Menu menu, Integer quantity, Integer unitPrice) {
        return OrderItem.builder()
            .order(order)
            .menu(menu)
            .quantity(quantity)
            .unitPrice(unitPrice)
            .build();
    }
}
