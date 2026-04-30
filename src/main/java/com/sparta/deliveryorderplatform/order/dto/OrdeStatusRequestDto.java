package com.sparta.deliveryorderplatform.order.dto;

import com.sparta.deliveryorderplatform.order.entity.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrdeStatusRequestDto {
    private OrderStatus status;
}
