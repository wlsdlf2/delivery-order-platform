package com.sparta.deliveryorderplatform.order.dto;

import com.sparta.deliveryorderplatform.order.entity.OrderStatus;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderSearch {  //검색 요청의 파라미터를 받기 위한 객체
    private final UUID storeId; // 가게별 검색 - 사용자 OWNER
    private final OrderStatus status; // 주문 상태 - 주문 상태 - CUSTOMER

    //final을 붙힌 이유: 객체의 상태를 변하지 않게 하기 위함.
    // 객체에 값을 담기만 하고, 비즈니스 로직에서 변경하지 않기 위함이다.
}
