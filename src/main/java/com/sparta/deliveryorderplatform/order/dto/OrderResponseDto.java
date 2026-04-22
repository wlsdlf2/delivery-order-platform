package com.sparta.deliveryorderplatform.order.dto;

import com.sparta.deliveryorderplatform.order.entity.Order;
import com.sparta.deliveryorderplatform.order.entity.OrderStatus;
import com.sparta.deliveryorderplatform.order.entity.OrderType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private UUID orderId;
    private String customerId;
    private UUID storeId;
    private UUID addressId;
    private OrderType orderType;
    private OrderStatus status;
    private Integer totalPrice;
    private String request;

    private LocalDateTime createdAt;

    //OrderResponse 객체로 변환하는 빌더 만들 예정
    public static OrderResponseDto from(Order order) {
        return OrderResponseDto.builder()
            .orderId(order.getId())
            .addressId(order.getAddress().getId())
            .customerId(order.getUser().getUsername())
            .storeId(order.getStore().getId())
            .orderType(order.getOrderType())
            .status(order.getStatus())
            .totalPrice(order.getTotalPrice())
            .build();
    }
}
