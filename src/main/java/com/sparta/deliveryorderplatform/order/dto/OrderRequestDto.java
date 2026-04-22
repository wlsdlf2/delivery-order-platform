package com.sparta.deliveryorderplatform.order.dto;

import com.sparta.deliveryorderplatform.order.entity.OrderItem;
import com.sparta.deliveryorderplatform.order.entity.OrderStatus;
import com.sparta.deliveryorderplatform.order.entity.OrderType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderRequestDto {
    @NotNull(message = "가게는 반드시 지정되어야 합니다.")
    private UUID sotreId;

    @NotNull(message = "주소는 반드시 지정해야 합니다.")
    private UUID addressId;

    @NotNull(message = "주문 유형을 선택해주세요!!")
    private OrderType orderType;

    private String request;

    @NotEmpty(message = "주문할 메뉴가 1개 이상이어야 합니다.")
    private List<OrderItemRequestDto> items;
}
