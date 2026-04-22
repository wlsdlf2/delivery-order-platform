package com.sparta.deliveryorderplatform.order.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderItemRequestDto {
    @NotNull(message = "메뉴를 선택해야합니다.")
    private UUID menuId;        // 메뉴 식별자
    @NotNull(message = "수량을 선택해주세요.")
    private Integer quantity;   // 메뉴 수량
}
